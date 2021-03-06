/*
 * Copyright 2017 FBK/CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.auth.service.services;

import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.service.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    public class TokenHandlingException extends RuntimeException {

        public TokenHandlingException(Throwable cause) {
            super(cause);
        }
    }

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Autowired
    private JwtTokenService tokenUtil;

    @Autowired
    private TokenRepository tokenRepository;

    public Iterable<Token> list(String uuid) {
        return tokenRepository.findByUserUuid(uuid);
    }

    public Token read(Long tokenId) {
        return tokenRepository.findOne(tokenId);
    }

    public void delete(Token token) {
        Token t2 = tokenRepository.findOne(token.getId());
        if (t2 == null) {
            return;
        }
        tokenRepository.delete(t2.getId());
    }

    public Token update(Token rawToken) {

        Token token = read(rawToken.getId());
        if (token == null) {
            return null;
        }

        token.setName(rawToken.getName());

        tokenRepository.save(token);
        return token;
    }

    public Token create(Token token) {
        tokenRepository.save(token);
        return token;
    }

    public Token read(String authToken) {
        if (authToken == null) {
            return null;
        }
        return tokenRepository.findByToken(authToken);
    }

    @Retryable(maxAttempts = 5, value = TokenHandlingException.class, backoff = @Backoff(delay = 200, multiplier = 4))
    public Token createLoginToken(User user) {
        
        Token token = tokenUtil.createToken("login", user, this.expiration, this.secret);
        
        // Handle high concurrency
        Token storeToken = tokenRepository.findByToken(token.getToken());
        if(storeToken != null && storeToken.getType().equals(Token.Type.LOGIN)) {
            
            if(storeToken.isValid()) {
                return storeToken;
            }
            
            // any other case just drop the previous one refresh the new one
            delete(storeToken);
            tokenUtil.refreshToken(token);
        }
        
        token.setSecret(Token.Type.LOGIN.name());
        token.setType(Token.Type.LOGIN);

        try {
            create(token);
            return token;
        } catch (DataIntegrityViolationException e) {
            logger.warn("Failed to store the token, trying to regenerate");
            throw new TokenHandlingException(e);
        }
    }

    public Token refreshToken(Token token) {
        tokenUtil.refreshToken(token);
        return tokenRepository.save(token);
    }

    public Token generateToken(Token token) {
        token.setToken(tokenUtil.generateToken(token));
        return token;
    }

    public boolean isValid(Token token, String secret) {
        // Cannot read the token claims?
        if (tokenUtil.getClaims(token, secret) == null) {
            return false;
        }
        return token.isValid();
    }

    public boolean isValid(Token token) {
        if (token == null) {
            return false;
        }

        // on LOGIN token type use the internal secret
        String tokenSecret = (token.getSecret().equals(Token.Type.LOGIN.name())) ? this.secret : token.getSecret();
        return isValid(token, tokenSecret);
    }

}
