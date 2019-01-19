# BookingGo_Test

## Setup

I have used the following external libraries.

json-20180813

commons-cli-1.3.1

com.sparkjava:spark-core:Release

However, I have packaged up the code into executable jars, so you shouldn't have to download anything to run the program. Simply download Console.jar and RestAPI.jar.

## Part 1

### Command to print the results from the supplier APIs
`java -jar Console.jar --pickup 51.470020,-0.454295 --drop 51.470020,-0.454295`

### Command to specify the number of passengers
`java -jar Console.jar --pickup 51.470020,-0.454295 --drop 51.470020,-0.454295 --passengers 3`

## Part 2
### Command to start the REST API
`java -jar RestAPI`

The RestAPI can be accessed through http://localhost:4567/

This page will also display a help message, including the example queries below:

http://localhost:4567/prices?dropoff=51.470020,-0.454295&pickup=52.167241,-0.443187

http://localhost:4567/prices?pickup=52.167241,-0.443187&dropoff=51.470020,-0.454295&passengers=5
