
#cd ~/raptor
#sudo -u raptor raptor -launch

sudo killall -u raptor
sudo service raptor restart
tail -f /var/log/raptor/debug.log
