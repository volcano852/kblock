# Curl commmands

## Add signed transaction
`curl -H "Accept: application/json" -H "Content-type: application/json" -X POST -d '{"sender":"GJ9AA0vnoZnx+piRZo7jq2BJ+C04xovnD1luqy4YV7c=" , "receiver":"GJ9AA0vnoZnx+piRZo7jq2BJ+C04xovnD1luqy4YV7c=" , "amount" : 120.0, "signature" : "GJ9AA0vnoZnx+piRZo7jq2BJ+C04xovnD1luqy4YV7c=" , "random" : 123}' http://localhost:8080/transactions`