package burp;

import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import spec.extender.CONST;
import spec.extender.exception.ExtenderException;
import spec.extender.hmac.updater.UpdaterFactory;
import spec.extender.hmac.updater.UpdaterPayload;
import spec.extender.util.StringUtil;
import spec.extender.util._debug;

public class BurpExtender extends AbstractTableModel implements IBurpExtender, ITab, IHttpListener, IMessageEditorController
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private IBurpExtenderCallbacks 	callbacks;
    private IExtensionHelpers 		helpers;
    private JSplitPane 				splitPane;
    private IMessageEditor 			requestViewer;
    private IMessageEditor 			responseViewer;
    private IHttpRequestResponse 	currentlyDisplayedItem;
    private final List<LogEntry> 	log 
    								= new ArrayList<LogEntry>();
 
    private UpdaterFactory headerUpdaterFactory	
    								= new UpdaterFactory();
    //
    // implement IBurpExtender
    //
    
    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks)
    {
        // keep a reference to our callbacks object
        this.callbacks = callbacks;
        
        // obtain an extension helpers object
        helpers = callbacks.getHelpers();
        
        // set our extension name
        callbacks.setExtensionName("SOS Logger");
        
        // create our UI
        SwingUtilities.invokeLater(new Runnable() 
        {
            @Override
            public void run()
            {
                // main split pane
                splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                        
                // table of log entries
                Table logTable = new Table(BurpExtender.this);
                JScrollPane scrollPane = new JScrollPane(logTable);
                splitPane.setLeftComponent(scrollPane);

                // tabs with request/response viewers
                JTabbedPane tabs = new JTabbedPane();
                requestViewer = callbacks.createMessageEditor(BurpExtender.this, false);
                responseViewer = callbacks.createMessageEditor(BurpExtender.this, false);
                tabs.addTab("Request", requestViewer.getComponent());
                tabs.addTab("Response", responseViewer.getComponent());
                splitPane.setRightComponent(tabs);

                // customize our UI components
                callbacks.customizeUiComponent(splitPane);
                callbacks.customizeUiComponent(logTable);
                callbacks.customizeUiComponent(scrollPane);
                callbacks.customizeUiComponent(tabs);
                
                // add the custom tab to Burp's UI
                callbacks.addSuiteTab(BurpExtender.this);
                
                // register ourselves as an HTTP listener
                callbacks.registerHttpListener(BurpExtender.this);
            }
        });
    }

    //
    // implement ITab
    //

    @Override
    public String getTabCaption()
    {
        return "Logger";
    }

    @Override
    public Component getUiComponent()
    {
        return splitPane;
    }

    //
    // implement IHttpListener
    //
    
    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo)
    {
    	//process intruder events
    	if (messageIsRequest && 
    			(toolFlag == IBurpExtenderCallbacks.TOOL_INTRUDER || toolFlag == IBurpExtenderCallbacks.TOOL_REPEATER)
    		)
    	{
			IExtensionHelpers helpers 	= callbacks.getHelpers();
			_debug.println("\n===============on proxy===============\n");
			IRequestInfo requestInfo	= helpers.analyzeRequest(messageInfo);
			
	    	String burpRequestURL 		= requestInfo.getUrl().toString();
	    	String requestURL			= burpRequestURL.substring(0, burpRequestURL.length() -1);
	    	_debug.println("--------burp request url--------\n" + burpRequestURL);			
	    	 	
	    	List<String> headers 		= requestInfo.getHeaders();	    	
	    	_debug.println("--------original request headers to be updated-------\n" 
	    								+ StringUtil.listToStringLines(headers));	
	    	
			byte[] request 				= messageInfo.getRequest();
			String requestBody 			= new String(request);
			requestBody 				= requestBody.substring(helpers.analyzeRequest(request).getBodyOffset());	
			_debug.println("--------original request body-------\n"
										+ requestBody);
			
			UpdaterPayload	payload		= new UpdaterPayload(headers, requestBody);	
			UpdaterPayload updatedPayload
										= headerUpdaterFactory.create(requestURL).doUpdate(payload);
	    					    		
			List<String> updatedHeaders	= updatedPayload.getHeaders();
			String updatedBody			= updatedPayload.getRequestBody();
			byte[] updatedRequest 		= helpers.buildHttpMessage(updatedHeaders, helpers.stringToBytes(updatedBody));
			
			_debug.println("\n----[updated full request]\n" + helpers.bytesToString(updatedRequest));
			
			/** make the updated request */
			IHttpService httpService 	= messageInfo.getHttpService();
			
			callbacks.makeHttpRequest(httpService, updatedRequest);
		}
    	
        // only process responses
        if (!messageIsRequest)
        {
            // create a new log entry with the message details
            synchronized(log)
            {
                int row = log.size();  
                _debug.println("log row: " + row);
                log.add(new LogEntry(row, toolFlag, callbacks.saveBuffersToTempFiles(messageInfo), 
                        helpers.analyzeRequest(messageInfo).getUrl()));
                fireTableRowsInserted(row, row);
            }
        }
    }

    //
    // extend AbstractTableModel
    //
    
    @Override
    public int getRowCount()
    {
        return log.size();
    }

    @Override
    public int getColumnCount()
    {
        return spec.extender.CONST.NUM_COL;
    }

    @Override
    public String getColumnName(int colIndex)
    {
    	return spec.extender.CONST.COL_NAME[colIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        LogEntry logEntry = log.get(rowIndex);
        
        byte[] response 		= logEntry.requestResponse.getResponse();
        IResponseInfo resInfo	= helpers.analyzeResponse(response);
        List<String> resHeaders	= resInfo.getHeaders();
//        Util.debug("\nResHeader:\n" + Util.listToString(resHeaders));
        
        
        switch (columnIndex)
        {
        	case 0:
        	    return logEntry.index;
            case 1: 
                return callbacks.getToolName(logEntry.tool);
            case 2:
                return logEntry.url.toString();
            case 3: 
            	return resInfo.getStatusCode();   
            case 4:
            	return response.length;
            case 5:{
            	return getAResponseHeader(resHeaders, CONST.HEADER_DATE);
            }            	
            default:
                return "";
        }
    }

    private String getAResponseHeader(List<String> resHeaders, String headerName){
    	for (String h: resHeaders){
    		if (h.startsWith(headerName)) return h;
    	}
    	
    	throw new ExtenderException("No header found the in response having the name: " + headerName);
    }
    //
    // implement IMessageEditorController
    // this allows our request/response viewers to obtain details about the messages being displayed
    //
    
    @Override
    public byte[] getRequest()
    {
        return currentlyDisplayedItem.getRequest();
    }

    @Override
    public byte[] getResponse()
    {
        return currentlyDisplayedItem.getResponse();
    }

    @Override
    public IHttpService getHttpService()
    {
        return currentlyDisplayedItem.getHttpService();
    }

    //
    // extend JTable to handle cell selection
    //
    
    private class Table extends JTable
    {
        public Table(TableModel tableModel)
        {
            super(tableModel);
        }
        
        @Override
        public void changeSelection(int row, int col, boolean toggle, boolean extend)
        {
            // show the log entry for the selected row
            LogEntry logEntry = log.get(row);
            requestViewer.setMessage(logEntry.requestResponse.getRequest(), true);
            responseViewer.setMessage(logEntry.requestResponse.getResponse(), false);
            currentlyDisplayedItem = logEntry.requestResponse;
            
            super.changeSelection(row, col, toggle, extend);
        }        
    }
    
    //
    // class to hold details of each log entry
    //
    
    private static class LogEntry
    {
    	final int index;
        final int tool;
        final IHttpRequestResponsePersisted requestResponse;
        final URL url;

        LogEntry(int index, int tool, IHttpRequestResponsePersisted requestResponse, URL url)
        {
        	this.index	= index;
            this.tool 	= tool;
            this.requestResponse 
            			= requestResponse;
            this.url 	= url;
        }
    }
}
