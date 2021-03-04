
"use strict";
const express=require('express');
const bodyParser = require('body-parser');
let app=express();
const fs = require('fs');
const PORT = 8080;
//openWether key
const API_key = "2d49bf528cf4ca56c119aabb471ad948" 

const FCM = require('fcm-push');

const FCM_SERVER_KEY = 'FCM API_key'
const MongoClient = require('mongodb').MongoClient;
const MONGO_URL = "mongodb://localhost:27017";

const client = new MongoClient(MONGO_URL, {
    useNewUrlParser: true,
    useUnifiedTopology: true
});
let timerId;
let tokentest = "test token"
let fcm = new FCM(FCM_SERVER_KEY);

app.use(bodyParser.json());


app.post('/:user/token', (req, res, next) => {
	console.log("got a request to update token")
	let token = req.body.token;
	client.connect()
			.then(() => {let db = client.db('mongotestdb');
		    let collection = db.collection('users');
		    console.log(req.params.user)
		    let it = collection.find({username: req.params.user})
		    it.count((err, count)=>{
		    	console.log(count);
		    	if(count >= 1){
		    		 it.forEach((user)=>{
				    	if (user.Token != token){
				    		console.log(user.token);
				    		let id = user._id;
				    		collection.update({_id:id},{$set:{Token:token}})
				    		console.log("insert new token");
				    		res.status(200).json({msg: `update user token successfully `});
				    	}
				    	else{
				    		res.status(200).json({msg: `token is allready update `});
				    	}
		    		})
		    	}
		    	else{
		    		res.status(200).json({msg: `no user found with the name ${req.params.user}`});
		    	}
		    })
		    });
});



/*insert a new user to db
	get a post request with all the needed data
	insert new user to DB and check if the insert Succeeded
*/
app.post('/:user/newuser', (req, res, next) => {
	console.log(req);
    let email = req.body.email;
    let firstName = req.body.firstname;
    let lastName = req.body.lastname;
    let Password = req.body.password;
    let token = req.body.token;

    console.log(`Received save new user request of ${email}`);

   // if (!token) return res.status(400).json({err: "missing token"});
    //let targetToken;
    client.connect()
		.then(() => {
		    let db = client.db('mongotestdb');
		    let collection = db.collection('users');
		    return collection.insert([
		        {username: email , firstName : firstName , lastName: lastName ,Password : Password, Token: token }, 
		    ]);
		})
		.then(() => {let db = client.db('mongotestdb');
		    let collection = db.collection('users');
		    let targetToken = collection.find( {username: email})
		   })
		.catch(err => {
			res.status(200),json({msg: "can't save new user", added: "false"})
		    console.error(err);
		});
		console.log("saved ok" );
    res.status(200).json({msg: "saved ok", added: "true"});
});


/* check if a user is in the db
	get a post request with user name (email) and password
	a check if it is exsists in the DB
	if so , return ok
	if not , return error

*/
app.post('/:user/check', (req, res, next) => {
    let email = req.body.email;
    //let firstName = req.body.firstname
    //let lastName = req.body.lastname
    let Password = req.body.password

    console.log(`Received request to log in from ${email}`);
    console.log(`Password :  ${Password}`);
   // if (!token) return res.status(400).json({err: "missing token"});
    //let targetToken;
    client.connect()
		.then(() => {let db = client.db('mongotestdb');
		    let collection = db.collection('users');
		    collection.find({username: email , Password : Password}).count((e,r)=>{
		    	console.log(r)
		    	if(r >0){
		    		res.status(200).json({msg: "username and password match", match: "true"});
		    	}
		    	else {
		    		res.status(200).json({msg: "email or password dont match", match: "false"})
		    	}
		    	});
		    })
		/*.then((num)=>{ 
		    console.log(num);
			//callback(num);
		})*/
		   
		.catch(err => {
			res.status(200).json({msg: "error in server", match: "false"})
		    console.error(err);
		});
		//console.log("user found");
    //res.status(200).json({msg: "ok"});
});

/*  this fucn is  geting a wind speed in a loction  
	and if the wind speed is above the set point value
	it send a notification to the user
*/

function fetchwind(API_key,user,lat,lon,setPoint ,res) {
    const axios = require('axios');
	axios.get(`http://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&units=metric&appid=${API_key}`)
	  .then(response => {
   		client.connect()
		.then(() => {
			if (response.data["wind"] > setPoint) {
				console.log("good time to surf! sending notification!")
				let db = client.db('mongotest');
			    let collection = db.collection('users');
			    let it = collection.find({username: user})
			    /*it.forEach((token)=>{
			    	fcm.send({
			        to: token.token,
			        data: {
			            someKey: "some value"
			        },
			        notification: {
			            title: "message title",
			            body: response.data["Global Quote"]["05. price"]
			        }
			    	});	
			    })
			    console.log("send notification message to app")*/
			    clearInterval(timerId)
			 }
		   });
	    return
	  })


	  .catch(error => {
	    console.log(error);
  	});
}


/* return the current weather for a specific place
	the call get Latitude & Longitude of a specific place
	and run a api request.
	the call return a json with wind speed , wind deg and temperature
*/
app.post('/:user/now',(req, res,next)=>{
	let lat = req.body.lat;
	let lon = req.body.lon;
	const axios = require('axios');
	axios.get(`http://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&units=metric&appid=${API_key}`)
	  .then(response => { 
	  	console.log(response.data["wind"])
	  	return res.status(200).json({wind :response.data["wind"], temp : response.data["main"]["temp"] })
	  })
	});

/* return a 7 day forecast weather for a specific place
	the call get Latitude & Longitude of a specific place
	and run a api request.
	the call return a json with wind speed , wind deg and temperature for each day
*/
app.post('/:user/forecast',(req, res,next)=>{
	let lat = req.body.lat;
	let lon = req.body.lon;
	const axios = require('axios');
	axios.get(`http://api.openweathermap.org/data/2.5/onecall?lat=${lat}&lon=${lon}&units=metric&appid=${API_key}`)
	  .then(response => { 
	  	console.log(response.data["daily"][1]["dt"]);
	  	let msg = {};
	  	for( let i = 1 ; i < 8 ; i++){
	  			let key1 = "day_" + i;
	  			let key2 = "wind_speed_" + i;
	  			let key3 = "wind_deg_" + i;
	  			let key4 = "temp_" + i;
	  			let temp = response.data["daily"][i]["dt"];
	  			let value1 = new Date(temp * 1000);
	  			value1 = value1.toLocaleString();
	  			let value2 = response.data["daily"][i]["wind_speed"];
	  			let value3 = response.data["daily"][i]["wind_deg"];
	  			let value4 = response.data["daily"][i]["temp"]["day"];
	  			console.log(key1)
	  			console.log(value1)
	  			msg[key1] = value1;
	  			msg[key2] = value2;
	  			msg[key3] = value3;
	  			msg[key4] = value4;
	  			}
	  	return res.status(200).json(msg)
	  })
	});


/*  this func start a intreval that check id the wind speed
	is above the set point 
	if so , it wiil send notification to the user
*/
app.post('/:user/start', (req, res, next) => {
	let lat = req.body.lat;
	let lon = req.body.lon;
	let setPoint = req.body.setPoint
    console.log("Got POST request to start checking wind speed periodic");
    console.dir(req.body);
    timerId = setInterval(fetchwind, 10000,  API_key, req.params.user,lat,lon,setPoint);
    return res.json({successe :" successfully start "});
});

/*  this func stop the  intreval that check id the wind speed
	is above the set point 
*/
app.post('/:user/stop', (req, res, next) => {
    console.dir(req.body);
    console.log("Got POST request to stop checking wind speed periodic");
    clearInterval(timerId)
    return res.json({successe :" successfully stop "});
});

app.get('/test', (req, res, next) => {
    //console.dir(req.body);
    //console.log("Got POST request to stop checking wind speed periodic");
    //clearInterval(timerId)
    fcm.send({
		        to: tokentest,
		        data: {
		            someKey: "some value"
		        },
		        notification: {
		            title: "message title",
		            body: "testttt"
		        }
		    	},(err, response) => {
        if (err) return res.status(500).json({err: `message sending failed - ${err}`});
        return res.status(200).json({msg: "sent ok"});
    });
   // return res.json({successe :" successfully  "});
});

app.listen(PORT,() => {
	console.log('Example app listening on port 8080!');

});