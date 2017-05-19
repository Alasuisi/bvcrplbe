package bvcrplbe.domain;

public class NotificationMessage {
	private int notificationType;
	private int userRole;
	private int transferID;
	private int relatedToTransfer;
	private String callBackURI;
	private String message;
	
	public NotificationMessage(){};
	
	public void setTypeNotification()
		{
		notificationType=0;
		}
	public void setTypeMessage()
		{
		notificationType=1;
		}
	public void setRoleDriver()
		{
		userRole=0;
		}
	public void setRolePassenger()
		{
		userRole=1;
		}
	public boolean isMessage()
		{
		return notificationType==1;
		}
	public boolean isNotification()
		{
		return notificationType==0;
		}
	public boolean isPassenger()
		{
		return userRole==1;
		}
	public boolean isDriver()
		{
		return userRole==0;
		}
	public void setCallBackURI(String url)
		{
		callBackURI=url;
		}
	public String getCallBackURI()
		{
		return callBackURI;
		}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getTransferID() {
		return transferID;
	}

	public void setTransferID(int transferID) {
		this.transferID = transferID;
	}
	

	public int getRelatedToTransfer() {
		return relatedToTransfer;
	}

	public void setRelatedToTransfer(int relatedToTransfer) {
		this.relatedToTransfer = relatedToTransfer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callBackURI == null) ? 0 : callBackURI.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + notificationType;
		result = prime * result + relatedToTransfer;
		result = prime * result + transferID;
		result = prime * result + userRole;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NotificationMessage other = (NotificationMessage) obj;
		if (callBackURI == null) {
			if (other.callBackURI != null)
				return false;
		} else if (!callBackURI.equals(other.callBackURI))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (notificationType != other.notificationType)
			return false;
		if (relatedToTransfer != other.relatedToTransfer)
			return false;
		if (transferID != other.transferID)
			return false;
		if (userRole != other.userRole)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NotificationMessage [notificationType=" + notificationType + ", userRole=" + userRole + ", transferID="
				+ transferID + ", relatedToTransfer=" + relatedToTransfer + ", callBackURI=" + callBackURI
				+ ", message=" + message + "]";
	}

	
	
}
