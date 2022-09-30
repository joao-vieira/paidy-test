<img src="/paidy.png?raw=true" width=300 style="background-color:white;">

# Paidy Exercise (Incomplete)

## How to run
Write sbt compile and then sbt run to run the project. Currently compilation fails with an unresolved error.

##Key
In order to use the API Key to One-Frame, please add the following line to application.conf

api_key = "{Same as exercise}"

Remember to replace the string with the API key listed in the original exercise sheet (Forex.md)

#To Do
Many features were not implemented on time. After managing to compile successfully, some of the next steps would be:
1. Add tests
2. Add more descriptive errors when something goes wrong
3. In Currency.scala, conversion to and from string could be simplified
4. Better caching mechanism should be used (currently prices are just stored in a hashmap)
5. Delete price from hashmap after 5 minutes