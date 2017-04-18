# Syna.esth.etic

<br>
<br>
C̸͉̤͖̪̙̹͇͈̺̩̓̿̆̌͛͛̐̎̐͒̋̕͘͜͠o̷̧̢̯̰͖̻̺̹̦͚͍̩̾m̴̛͍̱̽m̷̯̜̭̠͇͖̣͔̾ạ̴̢͓̜͇͇͇̯̝̣̭̼̋͆̊ņ̵̛̘̪̝͚͖͇̰̜̣̄̄͐̓͒̇͘̚ď̴͕̹̈́͆̉̏̉͛͆̄̋̀̀̈́̆͜͝ ̴̡̡͕̟̯̲̮͈̟̜͍̤̝̅̏̚ͅL̸̨̬̻̲͙͍̖̜̝̜̭͚̺͍͙̂̍̄̈͐̔͛̊̀i̴̡̙͈̠̭͓̲̲̭̬̝͎̲͐̉̓̅̌̏͂ṅ̴̡̧̡̻͚̜͓͕͕̮͍̽̋̂̑́͋́̚͘͝͝ê̸̲͕̱̼̦̅̐͑͗͊̓̓̒̑̚̚ ̷̣̺̄͘Ǧ̴̹̫͕̙̩̖͍͙͎̤̖̙̳̗̳̒̑̌͐̐̑̆͋̆͊̈̓̏l̶̦̺̟͔̹̍̂̋̈́͠ĩ̷̛͕̖̝̣̒͑̌̌̒͝ͅt̷̡̛̺͓̭͕̾̋͑̾͋͝c̴̡̘̟͇̝͕̙͔̱̟͖̺̹͕̓́̿̊̈́̌͋͌̌h̸̛̞̉̉͊̐̒̅̆̓̎͌͝ȅ̸̜̰̺̗̻̻̦͍̗̬̻͍̩͇̯̿͋r̷̛̤̜͇̠̻̟͓͉̃̋́͂̓̅̍́͂͗̄͂̕ ̵̭͓̟̔͑͌͆̽͛̒͛̊̕̚͜͝͝͝Ę̷̙̝̪͕͖̘̯̘̱̐̏͂͊x̷̡̡͙̳̻̝̥̲͚̓̎͗͌́̓̂͂̅̀̋̐̏͝t̴̻̖͎͕͕͎̣̱̄́̇̓͌͊̀̑̓̿̏͝r̴̖̘̠͔̳̲͎̫̬̘͎̰̈͗̈́̆a̵̡͍̝͍̟̘̙̗̘̼͆͋o̸͍̝̤͓̭̣͙̣͍̘̹͓̅̌̓̈́̀́̈́̅͝r̴̢͕̬̻͖̆d̵̢͓̣̟͈̱̈́̐͋͠ị̵̖̭͈̻̰̦̜̃̊͐͐̈́͐̄ņ̴͖̦͉̠̤̲̬̭̲̞̮̱͆̆̀̀̎͗͌͂̌̈́̃̓͋͜ã̷̹͕̲̘̞̼̠͓͙̩̮͙͇̤͛̅́į̵̧̟̠͕͓͕̻̮̰͓̫̾̄r̸̭̩̦̉̃́̈̈́̀̔̒̏̈̓͘̚e̵̢̢̖̻̣͓̟͎̤̦̓̀̆͛̒͛͗͐̈͂̈́̐̎ͅ
<br>
<br>
<br>
<br>

Process images with the SoX, the Swiss Army knife of audio manipulation.

## Installation

```shell
npm install -g synaesthetic
```

## Requirements

You need to have both SoX (`sox`) and ImageMagick (`convert`) installed, it should look like this

```
$ sox --version
sox:      SoX v14.4.1

$ convert --version
Version: ImageMagick 6.8.9-9 Q16 x86_64 2017-03-14 http://www.imagemagick.org
Copyright: Copyright (C) 1999-2014 ImageMagick Studio LLC
Features: DPC Modules OpenMP
Delegates: bzlib cairo djvu fftw fontconfig freetype jbig jng jpeg lcms lqr ltdl lzma openexr pangocairo png rsvg tiff wmf x xml zlib
```

## Basic use

Pass synaesthetic an input file and an output file, followed by any arguments to SoX. Make sure the output file has a .png extension.

```
synaesthetic infile.jpg outfile.png treble +20
```

The
[Effects section of the SoX man page](http://sox.sourceforge.net/sox.html#EFFECTS)
(`man 1 sox`) is your friend! You can chain as many transformations as you like!

```
synaesthetic infile.jpg outfile.png treble +20 echoes 0.7 0.8 10s 1 11s 1 dcshift 0.4
```

## Scripted use

You can write scripts in ClojureScript for more advanced uses, see the
[examples/](https://github.com/plexus/syna.esth.etic/tree/master/examples)
directory for some examples.

``` clojurescript
;; your_script.cljs
(sox {:channels 2 :depth 8}
     (treble 21)
     (bass -1)
     (echos 0.7 0.8 "1s" 0.4 "1s" 1)
     (dcshift 0.2))
```

The [`syna.esth.sox` namespace](https://github.com/plexus/syna.esth.etic/blob/master/src/syna/esth/sox.cljs) contains a bunch of functions you can use. Pass your script in with the `-s` flag.

```
synaesthetic -s your_script.cljs infile.jpg outfile.png
```

You can have syna.esth.etic watch your script with the `-w` flag. This will
automatically re-run the script automatically re-run the it whenever it changes.
If your image viewer also auto-updates then this provides a really nice
workflow.

## Pipes

You can use `-` as a file name to either read from stdout, or write to stdout.

## Author

Plexus [Mastodon](https://toot.cat/@plexus) / [Patreon](http://www.patreon.com/plexus)
