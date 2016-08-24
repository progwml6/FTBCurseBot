#FTB Bot for CurseApp 

## Running Instructions:
* The bot requires the location of the config file as a command line argument such as -c /home/me/bot/myconf.conf
* A sample config is located in config/sample.conf
* a mongo database is not required, but without one the functionality of the bot will be very limited.


## updating the curse library
* the library will not be public or usable in open source projects until curse publicly releases the apis
    * when the api is public we will use maven repos for the library
* build the library using gradle
```
gradle build
```
* copy the main jar from the library to the bot's libs folder
```
cp build/libs/javacurseapi-0.0.1-9999999.jar ../ftbcursebot/libs/javacurseapi-0.0.1-9999999.jar
```