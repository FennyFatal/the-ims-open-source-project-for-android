package javax.microedition.ims.android.core;

interface IPublicationListener
{

    void publicationDelivered();
    
    void publicationDeliveryFailed();
    
    void publicationTerminated();

}