# SanavesaNet
This framework is based on my work in SE300 regarding Java Sockets. I have extended and generalized that code into a decent networking framework that can be used on any platform.

## Features

### Server
Able to start, stop, and listen for connections.
        
### Client
Able to connect and disconnect from a server.
        
### Sending & Receiving
Send and receive entire objects with custom serialization, with handlers on both the client and server.
        
### Multithreading
The server is listening for connections on a separate thread. Each client connection has its own thread to prevent bottlenecking. All the code (that I know of) is threadsafe.
        
### Extensible
You can integrate this networking framework into your projects in minutes.
        
### Flexibility
If the standard Server/Client classes aren't enough for you, you can extend those classes and override the all kind of methods. There are callbacks for lots of events.
       
## Authors
* **Mohammad Alali** - *Core Development* - [Sanavesa](https://github.com/Sanavesa)

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
