# BookingGo_Test

## Part 1
### Command to print the results from the supplier APIs
java Console -p 51.470020,-0.454295 -d 51.470020,-0.454295

### Command to specify the number of passengers
java Console -p 51.470020,-0.454295 -d 51.470020,-0.454295 -n 5

## Part 2
### Command to start the REST API
java RestAPI

The RestAPI can be accessed through http://localhost:4567/
This page will also display a help message, including the example queries below:
http://localhost:4567/prices?dropoff=51.470020,-0.454295&pickup=52.167241,-0.443187
http://localhost:4567/prices?pickup=52.167241,-0.443187&dropoff=51.470020,-0.454295&passengers=5
