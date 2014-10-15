package burp;

/**
 * 
 When an extension registers itself as an Intruder payload provider, 
 this will be available within the Intruder UI for the user to select as the payload source for an attack. 
 When an extension registers itself as a payload processor, 
 the user can create a payload processing rule and select the extension's processor as the rule's action.
 
 When Burp calls out to a payload provider to generate a payload, 
 it passes the base value of the payload position as a parameter. 
 This allows you to create attacks in which a whole block of serialized data is marked as the payload position, 
 and your extension places payloads into suitable locations within that data, 
 and re-serializes the data to create a valid request. 
 Hence, you can use Intruder's powerful attack engine to automatically manipulate input deep within complex data structures.
 
 This example is artificially simple, and generates two payloads: 
 one to identify basic XSS, 
 and one to trigger the ficititious vulnerability that was used in the previous custom scanner check example. 
 It then uses a custom payload processor to reconstruct the serialized data structure around the custom payload.

 *
 */
public class BurpExtender implements IBurpExtender, IIntruderPayloadGeneratorFactory, IIntruderPayloadProcessor
{
    private IExtensionHelpers helpers;
    
    // hard-coded payloads
    // [in reality, you would use an extension for something cleverer than this]
    private static final byte[][] PAYLOADS = 
    {
        "|".getBytes(),
        "<script>alert(1)</script>".getBytes(),
    };

    //
    // implement IBurpExtender
    //
    
    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks)
    {
        // obtain an extension helpers object
        helpers = callbacks.getHelpers();
        
        // set our extension name
        callbacks.setExtensionName("Custom intruder payloads");
        
        // register ourselves as an Intruder payload generator
        callbacks.registerIntruderPayloadGeneratorFactory(this);
        
        // register ourselves as an Intruder payload processor
        callbacks.registerIntruderPayloadProcessor(this);
    }

    //
    // implement IIntruderPayloadGeneratorFactory
    //
    
    @Override
    public String getGeneratorName()
    {
        return "My custom payloads";
    }

    @Override
    public IIntruderPayloadGenerator createNewInstance(IIntruderAttack attack)
    {
        // return a new IIntruderPayloadGenerator to generate payloads for this attack
        return new IntruderPayloadGenerator();
    }

    //
    // implement IIntruderPayloadProcessor
    //
    
    @Override
    public String getProcessorName()
    {
        return "Serialized input wrapper";
    }

    @Override
    public byte[] processPayload(byte[] currentPayload, byte[] originalPayload, byte[] baseValue)
    {
        // decode the base value
        String dataParameter = helpers.bytesToString(helpers.base64Decode(helpers.urlDecode(baseValue)));
        
        // parse the location of the input string in the decoded data
        int start = dataParameter.indexOf("input=") + 6;
        if (start == -1)
            return currentPayload;
        String prefix = dataParameter.substring(0, start);
        int end = dataParameter.indexOf("&", start);
        if (end == -1)
            end = dataParameter.length();
        String suffix = dataParameter.substring(end, dataParameter.length());
        
        // rebuild the serialized data with the new payload
        dataParameter = prefix + helpers.bytesToString(currentPayload) + suffix;
        return helpers.stringToBytes(helpers.urlEncode(helpers.base64Encode(dataParameter)));
    }
    
    //
    // class to generate payloads from a simple list
    //
    
    class IntruderPayloadGenerator implements IIntruderPayloadGenerator
    {
        int payloadIndex;
        
        @Override
        public boolean hasMorePayloads()
        {
            return payloadIndex < PAYLOADS.length;
        }

        @Override
        public byte[] getNextPayload(byte[] baseValue)
        {
            byte[] payload = PAYLOADS[payloadIndex];
            payloadIndex++;
            return payload;
        }

        @Override
        public void reset()
        {
            payloadIndex = 0;
        }
    }
}