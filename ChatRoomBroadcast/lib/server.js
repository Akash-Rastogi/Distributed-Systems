const express = require('express');

const app = express();
const server = require('http').createServer(app);
const io = require('socket.io')(server);

var mysql = require('mysql');

var db = mysql.createConnection({
    host     : 'localhost',
    user     : 'root',
    password : 'root',
    database : 'chatroom'
});

// connect to the MySQL server
db.connect(function(err) {
  if (err) {
    return console.error('error: ' + err.message);
  }
 
  let createTodos = `create table if not exists log(
                          user varchar(30),
                          message varchar(200),
                          TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP
                      )`;
});

exports.server = {
  run(port) {
    server.listen(port, () => {
      console.log('Server listening at port %d', port);
    });
  },
};

const users = new Set();

//Insert into DB......
function insertIntoDB(username, msg) {
  var post  = {'user': username, 'message': msg};
  var query = db.query('INSERT INTO log SET ?', post, function(err, result) {
    if (err) {
      console.log(post);
      db.end();
      return console.log(err.message);
    }
  });
}

io.on('connection', function onConnection(socket) {
  let username;

  socket.on('message', function onMessage(data) {
    insertIntoDB(username, data.text);
    const text = data.text;
    io.sockets.emit('message', { username, text });
  });

  // TODO: validate login!
  // TODO: check if user is already logged in!
  socket.on('login', function onLogin(data) {
    username = data.username;
    users.add(username);
    insertIntoDB(username, "System: User Login");
    io.sockets.emit('login', { username, users: Array.from(users) });
  });

  socket.on('typing', function onTyping() {
    socket.broadcast.emit('typing', { username });
  });

  socket.on('stop-typing', function onStopTyping() {
    socket.broadcast.emit('stop-typing', { username });
  });

  socket.on('disconnect', function onDisconnect() {
    users.delete(username);
    insertIntoDB(username, "System: User Logout");
    socket.broadcast.emit('logout', { username, users: Array.from(users) });
  });
});
