package spec.extender.hmac.updater;


public interface Updater {
	public String getHmacKey();
	public String getSignaturePlaceholder();
	public String getServiceBaseURL();
	public String getClientID();
	
	public UpdaterPayload doUpdate(UpdaterPayload updaterPayload);
	
	public String toString();
}
