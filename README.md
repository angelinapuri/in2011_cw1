# Project 2D#4 README
(A Peer-to-Peer Distributed Hash Table)

## Project Overview
This project is a Java implementation of a decentralised key-value storage system built on a peer-to-peer network. The system consists of two main components.
### Full Node:
A full node is a node in the P2P network that has both a name and an address. It handles multiple incoming connections, manages the network map and processes requests from other nodes.
### Temporary Node:
A temporary node is a node in the P2P network that only has a name. It runs put and get requests to full nodes.

## Project Structure
#### FullNode.java: 
Implements a full node which connects to the network, listens and handles incoming connections. The methods in this class make it act as a requester(TCP Client). It uses the name and address of the starting node provided to find nodes nearby and connect to them (by sending notify requests and adding them to the network map). Finally, it checks the availability of all the nodes in the network map (by sending an ECHO? request) and updates the map accordingly every 60 seconds.
#### TemporaryNode.java
Implements a temporary node which acts as a requester(TCP Client) in the network. It interacts with a full node to store and get key-value pairs. It also displays the list of nodes nearest to the HashID of the key.
#### NetworkMap.java
Represents the network map which stores the name and address of nodes. It also provides methods to add, remove and find nearest nodes.
#### DataStore.java
Represents the key-value store used by FullNode to store data in a thread-safe manner(by using ConcurrentHashMap).
#### ClientHandler.java
Implements the logic to handle start, echo, notify, nearest, put, store and end requests and process each command. It also updates the network map based on these node interactions.
#### HashID.java
Provides utility methods for computing HashIDs and computing distance between HashIDs.
#### NodeNameAndAddress.java
Represents the name and address of nodes in the network.

## Instructions to build and run
### Steps:
1. Rename the nodeName in FullNode.java(optional)

2. Navigate to the project directory
```bash
cd <project_directory>
```
3. Compile the Java source files
```bash
javac *.java
```
4. To run the temporary node:
a. For store method 
```bash
java CmdLineStore FUllNodeName FullNodeAddress KEY VALUE
```
b. For get method
```bash
java CmdLineGet FUllNodeName FullNodeAddress KEY
```
5. To run the full node:
```bash
java CmdLineFullNode startingNodeName startingNodeAddress your_IPAddress your_portNumber
```

## Current Functionalities
### Temporary Node:
* Connects to the 2D#4 network using the name and address of the starting node
* Searches the network for the full node with a hashID closest to the given hashID
* Stores a key-value pair in the network
* Retrives a value, given its key from the network
### Full Node:
* Connects to the 2D#4 network using the name and address of the starting node
* Handles multiple inbound connections
* Responds to START, ECHO?, PUT?, GET?, NEAREST?, NOTIFY? and END correctly.
* Implements passive mapping by adding and removing full nodes that it has interacted with.
* Implements active mapping by finding nodes in the network using the starting node(and NEAREST? requests), sending them NOTIFY? requests and adding them to the network map. It also sends an ECHO? request every 60 seconds to check availability of nodes in the network and updates the map(by removing the unresponsive nodes).
