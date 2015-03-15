This plugin for the Eclipse Memory Analyzer provides a tree map view for a heap dump. It provides an additional query in the "Visualizations" category to show the heap dump as a tree map.

It is currently in _early stages_ and _untested_ for large heap dumps; please don't expect too much. The usefulness of this kind of visualization is currently unclear - as far as I know something like this has not been done yet, reason enough to at least try.

Here is a screenshot:

![http://treemapmat.googlecode.com/svn/trunk/javadoc/screenshots/heaptreemap_small.jpg](http://treemapmat.googlecode.com/svn/trunk/javadoc/screenshots/heaptreemap_small.jpg)

View in [big](http://treemapmat.googlecode.com/svn/trunk/javadoc/screenshots/heaptreemap.png).

To obtain this view, click the "Open Query Browser" icon for an openend heap dump and select "Heap TreeMap" from the "Visualizations" category.

Navigation in the tree map is possible using left click (zoom in) and right click (zoom out). The selected rectangle (object) can be inspected by holding down the shift key while left clicking the rectangle.


What is being visualized? The complete heap, which is actually a directed graph, is first transformed into a weighted tree (a spanning tree of the graph where all nodes have weights). There are many ways of building the tree from the graph; the algorithm just chooses one from following all outgoing references from all GC roots once - if the resulting tree is the best suited one or not is not clear. The nesting as expressed by the tree is "true", but not "exclusive" (since the true references form a graph and not a tree).

Currently not supported but thinkable would be to build this weighted tree starting at a given object; in worst case this would cover all or large portions of the heap and thus would not be significantly more helpful than the currently realized complete view.

A word on the weights: Each node in the tree is representing an object of the heap dump. Each object in the heap has a certain size (it seems this can also be zero). The weighted tree is computed such that each node holds the sum of all weights of the nodes in its subtree, plus its own weight. The weights of leafs of the tree correspond to the object sizes in the heap dump.

The weighted tree of the heap dump is computed _completely_, however the actual visualization (the layout of the rectangles representing the relative size of the nodes in the tree) is limited to a depth of eight nesting levels (drill down is possible, starting a new layout process with depth eight with the selected node as the root node).