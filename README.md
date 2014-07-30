# Minimal Om Server Side Rendering example

To get the basics down.

# How it works

There are 3 components to this.

1) Clojurescript code running om  

2) Node server running clojurescript capable of running 1.  

3) Clojure server sending data to 2, and inlining the data in the html response.

# How to run

First build the cljs web stuff  
> lein cljsbuild auto om-server-render

Then build the node cljs server  
> lein cljsbuild auto server

Then start up the clojure server  
> lein ring server-headless


