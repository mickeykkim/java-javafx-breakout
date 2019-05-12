default: Breakout

JFX_PATH = "/Library/Java/Extensions/javafx-sdk-11.0.2/lib"
JFX_LIB = --module-path $(JFX_PATH)
JFX_MODULES = --add-modules=javafx.controls --add-modules=javafx.media

%: %.java
	javac $(JFX_LIB) $(JFX_MODULES) $@.java
	java -ea $(JFX_LIB) $(JFX_MODULES) $@

clean: 
	$(RM) *.class