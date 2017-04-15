;; Elisp stuff


(make-local-variable 'after-save-hook)

(setq after-save-hook
      (lambda ()
              (interactive)
              (shell-command "/home/arne/opt/bin/synaesthetic /home/arne/clj-projects/synaesthetic/jwcserai.jpg")
              (with-current-buffer (get-buffer "output.png")
                (revert-buffer t t))))



(setenv "SYNAESTHETIC_HOME" "/home/arne/clj-projects/synaesthetic")
