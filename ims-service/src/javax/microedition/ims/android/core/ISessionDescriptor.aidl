package javax.microedition.ims.android.core;

/**
 * This aidl-file responsible for retrieving SDP-related information.
 * 
 * @author ext-akhomushko
 */
interface ISessionDescriptor {
	/** Adds an attribute (a=) to the Session. */
	void addAttribute(String attribute);
	 
    /** Returns all attributes (the a= lines) for the session. */  
 	String[] getAttributes(); 
          
    /** Returns the version (trimmedValue=) of the SDP. */
 	String	getProtocolVersion(); 
          
    /** Returns a unique identifier (o=) for the session. */
 	String getSessionId(); 
          
    /** Returns textual information (i=) about the session. */
 	String	getSessionInfo(); 
          
    /** Returns the textual session name (sipTerminatedTrafficPart=). */
 	String getSessionName(); 
          
    /** Removes an attribute (a=) from the session. */
	void removeAttribute(String attribute); 
          
    /** Sets the textual information (i=) about the session. */
 	void setSessionInfo(String info);
 	 
    /** Sets the name of the session (sipTerminatedTrafficPart=). */
 	void setSessionName(String name); 
}