**** This document has been prepared to save you some time and get familiar with Dillon Le’s code for weather server ****
**** This manual was prepared by Mahsa Mirzargar - 09/19/2014 ****
 
Short Intro: This package will periodically connect to NOAA's server, download SREF ensemble, decode them, run contour band depth analysis for a (pre-)set of values. Through the webpage: http://weather-server.sci.utah.edu/public/index.php, one can look at the contour boxplot visualization for available choices.
 
Currently a single server will serve the back-end (downloading/decoding/processing) and also be the web-server. The server name (accessible via ssh) is:  weather-server.sci.utah.edu.
 
The packages needs to be installed before you can start:
 
- Java: The current version working on the server is jdk1.8.0_20
- ITK: The C code requires ITK library (the current version working on the server is 4.4.2)
- Cmake: To compile the contour band depth analysis code
- Apache: To set up the web server
- Php: Should be installed and enabled
- Wgrib: To decode grib files (distributed by NOAA)
 
The codes to be compiled/run:
 
- CBD: Contour Band Depth (C code) – Developed by Mahsa Mirzargar
- WeatherVaneManger (java code) – Developed by Dillon Lee
- WeatherVaneWebsite (php code) – Developed by Dillon Lee
 
The downloading, decoding and processing of the forecast data is done via the java code in 'WeatherVaneManager'. In order for this code to work, you need to make sure you have wgrib and CBD code installed (both of them are C codes and CBD requires ITK library).
 
wgrib provided by NOAA has a makefile to compile and the CBD code can be compiled using cmake and specifying the ITK build path. Both packages have a read me file that walks you through the compilation/usage. 
 
In order to make the java code run properly, a couple of modifications are required:
 
1) Set the RootApplciationPath: WeatherVaneManager code requires a hardcoded 'RootApplicationPath' that should be set in Run.java. Simply search for the variable name and you should be able to find it.
 
 
2) Setting up the right directory-structure: Every other path used in the code is relative to RootApplicationPath you set earlier and look likes as follow:
 
   -> RootApplicationPath/svg (a hierarchy of folders will be generated in this folder - based on date and model run - that includes all the generated svg files).
   -> RootApplicationPath/downloaded ( as above, a hierarchy based on date and model run will be generated in this folder that include all the grib files downloaded from NOAA's server)
   -> RootApplicationPath/processed (a hierarchy based on the date and model run that includes all the decoded grib files in ascii format)
   -> RootApplicationPath/application (this folder should include the CBD executable file – simply copy the ‘main’ after compiling CBD code in here)
 
Note: the required files by CBD code (such as lonLat*.txt, etc) should be in the RootApplicationPath so that the java code can access them.
 
I believe the java code will automatically create 'downloaded', 'processed' and 'svg' folder, but if you don't have the application folder and/or the executables and required files for CBD code, the java code will simply ignore generating the svg files without any error.
 
3) Setting the wgrib executable path: The path to wgrib executable should be hard coded in Simulation.java at line 380.
 
After taking these steps, you can either run the executable directly or create a runnable jar file and copy it on the server and run it using: java -Xmx256m -jar /path/to/jarfile.jar
 
Note that you need to have the lonLat files wherever the .jar file is running from otherwise the java code won't run the CBD code without any notification.
 
Note: The version I am sharing has hard-coded path as: /usr/local/weather/ where the supporting packages are installed currently.
 
In order to get the webpage up:
 
1) Install apache
2) Modify the hard coded svg paths in application/controllers/IndexControler.php (2 places)
3) Start apache (good luck with that on opensuse if you are not the root!)
4) Install and enable php (libapache2-mod-php5 and a2enmod php5) and make sure apache is recognizing php properly (you might need to restart apache - sudo /usr/sbin/apache2ctl (start/stop) )
4) For opensuse, put everything inside WeatherVaneWebsite into /srv/www/htdocs (the path might be different one different systems)

Note: You may need to the 'svg-container' size based on the resolution in: /public/styles/default.css


 
Good luck!
