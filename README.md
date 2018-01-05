#Camera Exposure Calculator

Camera Exposure Calculator app allows you calculate the perfect exposure for your photography.
You just have to input your current exposure (Aperture, shutter speed and ISO) and it will automatically calculate the exposure based on your new settings.
It supports to add an ND filter until 30 stop.
The apps displays the current EV and integrates a chronometer to help you calculate a perfect exposure.
A night mode feature is implemented when using it at dark.

No design pattern or architecture design has been used as it's a pretty simple app with just one activity.
 
Details about the [EV calculation](https://en.wikipedia.org/wiki/Exposure_value) :

      EV = aperture + shutter - ISO
      EV = log₂(N²) + log₂(1/t) - log₂(S/100)
      t = S*N²/100*2^EV
     
       where:
      - N, is the relative aperture (f-number)
      - t, is the exposure time ("shutter speed") in seconds[2]
      - 100, is the default ISO
      - S, is the new ISO
      
      
Details about the [calculation after an ND filter](http://www.vassilistangoulis.com/gr/?p=4958) is added:


    
      Tnd = T0 * 2^ND
     
      where:
      - ND is the Stop value of your ND filter
      - T0 is the Base shutter speed (without filter attached) in seconds
      - Tnd is the final exposure time
      
      
![alt tag](http://awoisoak.com/public/android/exposure_1.png)
![alt tag](http://awoisoak.com/public/android/exposure_2.png)
![alt tag](http://awoisoak.com/public/android/exposure_3.png)
![alt tag](http://awoisoak.com/public/android/exposure_4.png)
