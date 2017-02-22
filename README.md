
The GrayLog Stream Lookup (SLookup) Pipeline Processor function
---

Facilitates lookups within other streams using a field in the current stream as the value to match in the remote stream.
       
For example, say I have two streams, one contains some http logs with source IPs (E.g. src_ip) from internal hosts and the other stream contains information about the systems on the network such as IP address (E.g. ip_address), computer name (E.g. computer_name), MAC address (E.g. mac_address), OU, make/model, etc. 
       
The SLookup function makes it possible to retrieve the value of one of these fields in the remote stream where the value of src_ip matches the value of ip_address.

The thought behind this function is implementation of similar functionality to the VLOOKUP function in Excel.

Here is a rule that was created to lookup the value of IP address to enrich a Windows Event Log entry coming from WinLogBeat

```
rule "IP Lookup"
when
    has_field("winlogbeat_computer_name")
then
    //StreamID, Source Field, Destination Field, Return Field, Relative Time
    let system_info = slookup("58aba0cb3cbe8205e76c6145", "winlogbeat_computer_name", "computer_name", "ip_address", "14400");
    set_field("ip_address", to_ip(system_info));
end
```

Function Breadown
---

Function | Description
-------- | -----------
slookup(stream, srcField, dstField, rtnField, timeRange) : String | Conduct a lookup in a remote stream and return a field value based on a matching source field. Similar to VLOOKUP in Excel

Parameter | Type | Required | Description
--------- | ---- | -------- | -----------
stream  | String |  Y | The stream to look up the source field.
srcField | String | Y | The source field to lookup and match in the remote stream.
dstField | String | Y | The destination field that will be matched against with the source field.
rtnField | String |  Y | The field to return if there is a value match.
timeRange | String |  Y | Relative Time Range

Example plugin that implements a simple Graylog Pipeline Processor function.
---


Blog post explaining this project: https://www.graylog.org/blog/71-writing-your-own-graylog-processing-pipeline-functions
