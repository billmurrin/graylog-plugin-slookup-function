GrayLog Stream Lookup (SLookup) PipeLine Processor Function
============================================

The GrayLog Stream Lookup (SLookup) Pipeline Processor function facilitates lookups from within other streams using a field in the current stream and a remote field as the matching parameter.

For example, say I have two streams, one contains some source IPs from internal hosts and the other stream contains host information such as IP and MAC addresses. 

The SLookup function makes it possible to retrieve the value of MAC address from the other stream where it matches the IP address.

The thought behind this function is implementation of similar functionality to the VLOOKUP function in Excel.

Travis CI
---------

There is a `.travis.yml` template in this project which is prepared to automatically
deploy the plugin artifacts (JAR, DEB, RPM) to GitHub releases.

You just have to add your encrypted GitHub access token to the `.travis.yml`.
The token can be generated in your [GitHub personal access token settings](https://github.com/settings/tokens).

Before Travis CI works, you have to enable it. Install the Travis CI command line
application and execute `travis enable`.

To encrypt your GitHub access token you can use `travis encrypt`.

Alternatively you can use `travis setup -f releases` to automatically create a GitHub
access token and add it to the `.travis.yml` file. **Attention:** doing this
will replace some parts of the `.travis.yml` file and you have to restore previous
settings.
