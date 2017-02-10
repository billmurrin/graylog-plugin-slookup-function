
The GrayLog Stream Lookup (SLookup) Pipeline Processor function
---

Facilitates lookups from within other streams using a field in the current stream and a remote field as the matching parameter.
       
For example, say I have two streams, one contains some source IPs from internal hosts and the other stream contains host information such as IP and MAC addresses. 
       
The SLookup function makes it possible to retrieve the value of MAC address from the other stream where it matches the IP address.
       
The thought behind this function is implementation of similar functionality to the VLOOKUP function in Excel.

Example plugin that implements a simple Graylog Pipeline Processor function.
---


Blog post explaining this project: https://www.graylog.org/blog/71-writing-your-own-graylog-processing-pipeline-functions
