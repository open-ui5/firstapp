This folder contains one script ljsc.sh(.bat) and two jars in the lib directory

The script can be used to invoke commands of the LJS console from external process.

Commands that can be executed:
All the commands available in the OSGI console are also available in the Scriptable command client.
There are two additional commands: "startLJS" and "stopLJS".
"startLJS" lauches the LJS as it is configured in the "go" script. Currently it's reading the parameters from the "go.bat" file in the LJS distribution home directory.
If executes more than once consequently only the first call will start an LJS instance. Further prevent to start will be unsuccessful because the ports are occupied.
"stopLJS" is added just for symmetry - it calls "close" to the equinox Framework. Calling the "close" by the client has exactly the same effect as calling "stopLJS".

Details and example script
The script to run the scriptable commands client is called ljsc.sh(.bat) It depends on having java installed on the machine and in the path vaiable. The code of the client is java and contained in a jar file.

An example script would do some OS (file) operations, execute "startLJS"- and call some commands to install or start applications.
The parameters passed to the scriptable client are the command to be executed on the console and the command's parameters in the same order.

Here is one example for a script (MS Windows):

@echo starting the LJS if it is not started
call ljsc startLJS
call ljsc ss bundleA
copy file
call ljsc install://file
call ljsc ss bundleB

When the command finishes it's execution on the LJS the server a signal is passed back to the client and the client exits.
The control of the execution is returned back to the OS or the script and the next command in the script will be executed if any.

Warning: Command "startLJS" is an exception to this. It starts the process and just waits for a certain timeout then it returns the control. 
Meanwhile it catches the messages from the process stream and prints them to the OS. 
In the moment the "startLJS" exits the running state of the Equinox or any application is not guaranteed. 
This is a temporary solution and it is going to be enhanced by a well defined LJS state so the scripts that use the "startLJS" command will have the option to synchronize the rest of the operation with the state.

