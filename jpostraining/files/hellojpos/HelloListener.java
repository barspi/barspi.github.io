package org.jpos.training;

import org.jpos.iso.*;
import org.jpos.util.Log;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;

import java.util.Random;

@SuppressWarnings("unused")
public class HelloListener extends Log implements ISORequestListener, Configurable {
    private String greeting;

    @SuppressWarnings("unchecked")
    public void setConfiguration (Configuration cfg) throws ConfigurationException
    {
        greeting = cfg.get("greeting", "????");
    }

    public boolean process (ISOSource source, ISOMsg message) {
      // We receive 2 objects:
      //    * message: is the ISOMsg object
      //    * source: is where the message came from (a channel, or mux, etc)
      
      message.setResponseMTI();  // 0810
      
      
      Random random = new Random (System.currentTimeMillis());
      int rnd37= random.nextInt(1000000);           // RRN (Ref. Retrieval Number)
      int rnd38= random.nextInt(1000000);           // Approval Code
      
      message.set( 37, ""+rnd37 );
      message.set( 38, ""+rnd38 );
      
      String field4= message.getString(4);
      
      if ( "000000009999".equals(field4) )
          message.set (39, "01");
      else
          message.set (39, "00");
      
      message.unset(41);
      
      source.send (message);                        // Send the [transformed] original message
                                                    // back to where it came from (which is the
                                                    // QServer's XMLChannel)
      // THE END!! (for now)
      
      //--------------------------------------------------------------------------------
      
      Space sp= SpaceFactory.getSpace();            // Get a reference to the common Space (a TSpace)
      
      ISOMsg m2= (ISOMsg)message.clone();           // Create a clone of the message
      m2.setMTI("0800");
      
      m2.set(44, "Hello jPOS");                     // add some new field
      
      byte [] pinblock= new byte[] {0x10, 0xFF, 0xAA, 0x00, 0x22, 0x33, 0x99, 0xE2 };
      m2.set(52, pinblock);
      
      //       key         obj
      sp.out("queue-send", m2);                     // Send the clone to the queue
                                                    // (channel2 will take it from the queue
                                                    // and send it out the wire)
      // rsp= sp.in("queue-receive", 10000);
      
      return true;
    }
}
