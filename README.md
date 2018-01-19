# Synopsis

Shows how to connect to zookeeper from a scala program and how to use the features:

- group membership
- locks
- StateChangeListener

## Usage

Just start the program (with sbt run) and see what will happens. Then start another instance of it and then yet anoter...

Each instance will generate its unique ID and join a group in Zookeeper using this ID.
Each time an instance joins or leaves the same group a message will be printed by all the instances.

Then type a name of a lock in the console of any instance. The instance will create a lock with this name.
If you try the same name in another instance it will be blocked and wait at most 60 seconds.

But if you type the name of the lock again in the first instance it will release the lock and then the second instance will get it.

Type 'list' to get the list of all the locks that this instance holds.
Type 'exit' to close it.

## Akka

The application uses akka inside to react on the changes in group memebership and also to react on Zookeeper connection changes.

### Zookeeper server

There is a Docker composition in the directory `docker` which will bring up a Zookeeper server and a UI for it.

Just chagnge directory to `docker/zookeeper` and start it with `docker-compose up`

The UI will be accessible on http://localhost:19090

Username and password by default are "admin/manager"

