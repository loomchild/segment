# segment

## Introduction

Segment program is used to split text into segments, for example sentences.
Splitting rules are read from SRX file, which is standard format for this task. 

## Requirements

To run the project Java Runtime Environment (JRE) 1.5 is required.
To build the project from source Java Software Development Kit (JDK) 1.5 and Ant tool are required. 
Program should run on any operating system supported by Java. 
The helper startup scripts were written for Unix and Windows.

## Running

To run the program bin/segment script is used. 
For example on Linux, from main project directory, execute:

    bin/segment

On windows, from main directory, it looks like this:

    bin\segment

When the script does not work on your operating system program can be run 
directly using Java, look inside bin/split script for the clues how to do it.

Source text is read from standard input and resulting segments are written
on standard output, one per line. 
Without parameters text is split using simple, built-in rules.
To get help on command line parameters run:

    bin/segment -h

The most popular command line is probably:

    bin/segment -s rules.srx -l language -i in.txt -o out.txt

Where rules.srx is a file containing splitting rules, language is input file
language code, in.txt is a input file and out.txt is a output file.
To control output format useful parameters are -b and -e which define 
string that will be written before and after the segment (this replaces the 
standard end of line character).

## Performance

To evaluate performance bin/segment -p option can be used. It can measure
segmentation time on any data and it is possible to generate data.
To generate random text --generate-text option should be used with text length in kilobytes as a parameter. 
To generate random SRX --generate-srx option should be used with rule count and rule length separated by a comma as a parameter.
To repeat segmentation process -2 option should be used.
Other option which controls how the text is handled is -r which instructs the application to preload the whole text into memory before segmentation (some algorithms require it). 
Size of read buffer and therefore memory usage can be controlled by setting --buffer-length option.  
As a result of performance analysis segmentation time is displayed. Common usage example:

    bin/segment -p -2 --generate-text 100 --generate-srx 10,10

## Transformation

To automatically convert rule file between old SRX version and current SRX version there is a transformation tool, 
invoked by bin/segment -t command. By default it reads SRX from standard input and writes 
transformed SRX to standard output. Usage example:

    bin/segment -t -i old.srx -o new.srx

The tool accepts some command line parameters, use bin/segment -h for details.
Underneath it uses XSLT stylesheet which can be found in resources directory and used 
separately with any XSLT processor.

## Testing

The program has integrated unit tests. To run them execute:

    bin/segment --test

## Data formats

### Input

Plain text, UTF-8 encoded.

### Output

Plain text, UTF-8 encoded. Some operating system consoles, Windows 
command prompt for example, have different encoding and special characters 
will not be displayed correctly. Output files can be opened in text editors
because most of them handle UTF-8 encoded files correctly.
Each segment is prefixed with string set with -b option (empty by default), 
and suffixed with string set with -e option (new line character by default).

### SRX file

Valid SRX document as defined in SRX specification. 
Both version 1.0 and 2.0 are supported, although version 2.0 is preferred. 
Currently input is treated as plain text, formatting is not handled specially 
(contrary to specification). Example SRX files can be found in examples/ directory.

Document contains header and body. 

Header is currently mostly ignored, only "cascade" attribute is
read. It determines if only the first matching language rule is 
applied (cascade="no"), or all language rules that match language code 
are applied in the same order as they occur in SRX file (cascade="yes").

Body contains language rules and map rules. Language rules contain 
break (break="yes") and exception (break="no") rules. Each of those rules
can consist of two regular expression elements, &lt;beforebreak&gt; and 
&lt;afterbreak&gt;, which must match before and after break character respectively, 
for the rule to be applied.  
Map rules specify which language rules will be used to segment the text, 
according to the text language.

## Algorithm

The algorithm idea is as follows:

1. Rule matcher list is created based on SRX file and language. Each rule 
   matcher is responsible for matching before break and after break regular 
   expressions of one break rule.
   			
2. Each rule matcher is matched to the text. If the rule was not found the 
   rule matcher is removed from the list.
   			
3. First rule matcher in terms of its break position in text is selected.
			
4. List of exception rules corresponding to break rule is retrieved.
			
5. If none of exception rules is matching in break position then 
   the text is marked as split and new segment is created. In addition 
   all rule matchers are moved so they start after the end of new segment 
   (which is the same as break position of the matched rule).
			
6. All the rules that have break position behind last matched rule 
   break position are moved until they pass it.
			
7. If segment was not found the whole process is repeated.

In streaming version of this algorithm character buffer is searched. 
When the end of it is reached or break position is in the margin 
(break position > buffer size - margin) and there is more text, 
the buffer is moved in the text until it starts after last found segment. 
If this happens rule matchers are reinitialized and the text is searched again.
Streaming version has a limitation that read buffer must be at least as long 
as any segment in the text.

As this algorithm uses lookbehind extensively but Java does not permit
infinite regular expressions in lookbehind, so some patterns must be finitized. 
For example a* pattern will be changed to something like a{0,100}.

## Legacy algorithms

### Accurate algorithm

This is first implemented algorithm to perform segmentation task. It is stable but does 
not work on text streams and in real-world scenario with few break rules
and many exception rules it is several times slower than the other algorithms.

At the beginning the rule matcher list is created based on SRX file and language. 
Each rule matcher is responsible for matching before break and after break 
regular expressions of one rule (break or exception). 
Then each rule matcher is matched to the text. If the rule was not found the 
rule matcher is removed from the list. Next first matching rule (in terms of 
break point position) is selected. If it is break rule text is split. 
At the end all the rules that are behind last matched rule are matched until 
they pass it. The whole process is repeated until the matching rule was found
or there are no more rules on the list.

### Fast algorithm

This algorithm creates a single large regular expression incorporating all 
break rules. Then this regular expression is matched to the text. Every time
matching is found, all exception rules corresponding to this break rule 
are checked in this place. If no exception rules match, the text is split.

To create the streaming version of the algorithm ReaderCharacterSequence class 
was implemented. 
It implements character sequence interface but reads the text from a stream 
to the internal buffer. It does not work perfectly - buffer has limited size
so for example no all subsequences can be read from it.

As this algorithm uses lookbehind extensively but Java does not permit
infinite regular expressions in lookbehind, so some patterns are finitized. 
For example a* pattern will be changed to something like a{0,100}.

## Authors
* Jarek Lipski - creation of the project, design and programming
* Marcin Miłkowski - integration with LanguageTool, bugfixing

## Thanks
This project was written for Poleng company, but now is distributed as Free / Open Source Software. 
Results were used to write my Master's Thesis. Happy using:)

&nbsp;&nbsp;&nbsp;&nbsp;-- Jarek Lipski

