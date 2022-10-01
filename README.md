# Forex Assignment

## How to run
On a command line, type `sbt compile` to compile the program. Then, type `sbt run` to run the server. 

## How to make requests
By default, the server is running on 0.0.0.0:8081, although this value can be changed in application.conf.

Format for requests: "http://localhost:8081/rates?from={FROM_CURRENCY}&to={TO_CURRENCY}"

Where {FROM_CURRENCY} and {TO_CURRENCY} are your original and target currency, respectively.

## Before Running
In order to use the API Key to One-Frame, please add the following line to application.conf

api_key = "{Same as exercise}"

Remember to replace the string with the API key listed in the original exercise sheet (Forex.md)

Since One-Frame API uses Port 8080, I recommend changing the port value in application.conf as well.

# Description of the solution
In order to deal with the limitation of calls per day to the One-Frame service API, a cache system was implemented. After a pair-price combination is obtained, it is stored in cache, and returned whenever it's called again for 5 minutes. This value was chosen because it is stipulated in the assignment that rates should not be older than 5 minutes.

# To Do
Some features were left to implement due to time limitations. Here are some of the next tasks for this project:
1. Add tests for the caching mechanism, connection to One-Frame API, and many others
2. Add more descriptive errors when something goes wrong
3. In Currency.scala, conversion to and from string could be simplified
4. Better caching mechanism should be used (currently prices are just stored in a hashmap)
