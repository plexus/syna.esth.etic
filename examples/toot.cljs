(sox {:channels 1 :depth 8}
     ;; reverberation is the sound caused by the acoustics of a room, the result
     ;; of sound-waves bouncing off walls and surfaces. Play with different room
     ;; sizes (:room-scale), and with the time it takes for the first reflection
     ;; to arrive back (:pre-delay).
     ;;
     ;; A reverb effect mixes the dry signal (the original source) with the wet
     ;; signal (the sound of the reflections). You can have a look at just the
     ;; reflections with :wet-only, or give them a boost with :wet-gain.
     ;;
     ;; I have the input channels above set to "1", this gives the cleanest
     ;; reverb effect, but you get very different results when the audio is
     ;; treated as multi-channel (stereo, surround, etc). Try :channels 2, 3, 4,
     ;; 5, or even 10 or 20.
     (reverb {:wet-only false
              :hf-damping 0
              :room-scale 17
              :pre-delay 20
              :wet-gain 1.5})

     ;; If the result is too dark then shift it back a bit, or if it's too light
     ;; then use a negative dcshift
     (dcshift 0.2)

     ;; Now pump that treble way up. This really makes the colors pop, kind of
     ;; pop-arty effect.
     (treble 20)

     ;; pumping the bass makes certain patches look a bit darker and smudged
     (bass 6.1)
     )
