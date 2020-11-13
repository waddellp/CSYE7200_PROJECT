# World Earthquake Forecaster
Final Project for the "Big-Data Sys Engr Using Scala" [CSYE 7200] course at Northeastern University

__Team 3__<br>
Patrick Waddell [001058235]<br>
Rajendra kumar Rajkumar [001405755]

__Overview__<br>
Using publicly available data from the U.S. Geological Survey, create a tool to lookup historical data and forecast earthquakes. The tool will include a 10 year historical lookback and provide an interface with Google Maps to provide visualization of the information.

__Use Cases__
1.  Historical Lookup<br>A user enters latitude/longitude/radius and is provided with a Google Map / pins indicating earthquake events.
2.  Earthquake Forecast<br>A user enters an address and is provided with a percentage chance earthquakes at each magnitude.
3.  Top 10 Earthquake Hot Spots<br>A user enters a date range and is provided with the top ten locations around the globe for earthquake activity.

__Data Resource__<br>
[USGS – U.S.  Geological Survey](https://earthquake.usgs.gov/earthquakes/search/)

Data sets available through multiple formats:<br>
- xml, json, csv

Data sets include:<br>
- Latitude, longitude, magnitude, depth, geographical region, timestamp

~10,000 rows of data (+1..0 magnitude) per month~120,000 rows of data per year~1.2 million rows of data per decade
