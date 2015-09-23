# ChangeLog

## version 0.0 2006-02

* Project inception.	
  
## version 0.9 2006-03-28

* Basic functionality.
* Simple stream splitter and non-stream SRX splitter.
* Text interface.
* Performance tool.

## version 0.99 2008-06-17

* Completely rewritten SRX split algorithm - streaming and much faster.	
  
## version 1.0 2008-08-17
		
* Renamed project from Splitter to SRX Splitter to reflect the changes and emphasize SRX importance.
* Changed Splitter interface to TextIterator which implements Iterator. 
* Removed isReady method from Splitter interface to simplify code and remove
	unresolved bugs. Threads are better solution to non-blocking streams.
* Removed simple splitter - it can be easily replaced by SRX splitter with basic rules.
* Added support for SRX 2.0 in addition to SRX 1.0 along with transformer tool and XSLT stylesheet.
* Updated documentation and translated it to English.

## version 1.1 2009-04-15

* Renamed project from SRX Splitter to Segment.
* Changed project package from split to net.sourceforge.segment. 
* Added pattern caching.
* Fixed many bugs thanks to Marcin Miłkowski.
* Added buildnumber to the version.
	
## version 1.2 2009-05-28

* Changed minimum required Java version from 1.6 to 1.5 to make it work on Macs.
* Fixed many bugs thanks to Marcin Miłkowski - exception sometimes when text contains space at the end, rule skipping in legacy algorithm, initialization error on Macs and so on.
* Fixed a bug with break rule applying order - now the rule that will break first is applied independent of order.
* Changed console interface - now there is just one command named 'segment' to perform all the tasks.
* Added buildnumber to the version.
* Added debug information to the sources.
* Updated the documentation and shortly described the algorithms.
* Integrated loomchild-util library into this project, so it is no longer its dependency.

## version 1.3 2009-07-03

* Created brand new text iterator. It applies break rules in correct order (previous version was incorrect according to specification, algorithm pseudocode). It combines the ideas from previous algorithms so it is accurate and fast (even faster than old one).
* Fully integrated loomchild-util library code to segment package structure.
* Added preload, algorithm and output options to text interface.
* Renamed splitters to Fast, Accurate and Ultimate.
* Updated javadocs and documentation, described algorithms.
* Allowed map of additional parameters for text iterators. Added new options to text interface (lookbehind, buffer-length and margin).
* Created fast alternative SAX based SRX parser.
* Made SrxDocument thread safe.

## version 1.4 2012-05-13

* Started using Maven, reorganized project directory structure.
* Started using git.

## version 2.0 TBD

* Renamed root package from new.sourceforge.segment to net.loomchild.segment.
* Migrated to Github
* Library and UI in the same repository
