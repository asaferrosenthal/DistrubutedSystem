# DistrubutedSystem

Code is contained in folder 'Code'

Outputs are contained in folder 'Out'
- For frame size 3, outputs contained in folder 'Out3'
- For frame size 5, outputs contained in folder 'Out5'
- For frame size 7, outputs contained in folder 'Out7'
(Each file's output is Out<n>_<File_Name>.txt', n being the frame size)

For this project, each student was asked to realize an interprocess/interthread communication mechanism using Java Sockets and RMI to allow different processes/threads to communicate over a TCP/IP network.  The project also looked to compare 3 different page replacements algorithms, FIFO, LFU and MFU, on a particular computer server. Two clients would make request pages from a given server, which would either send it back (if it’s in its memory) or otherwise, a page fault occurs and the server requests the central server to swap in the missing page from the HDD. 
