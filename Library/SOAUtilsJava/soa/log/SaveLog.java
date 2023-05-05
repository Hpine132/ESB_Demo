package soa.log;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbUserException;

public class SaveLog extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly assembly) throws MbException {
		//MbOutputTerminal out = getOutputTerminal("out");
		//MbOutputTerminal alt = getOutputTerminal("alternate");
		Connection conn = null;
		CallableStatement cs = null;
		try {
			MbMessage message = assembly.getMessage();
			// ----------------------------------------------------------
			// Add user code below

			MbElement body = message.getRootElement().getLastChild();
			conn = getJDBCType4Connection("{ESBPolicy}:ESBDBPolicy",
					JDBC_TransactionType.MB_TRANSACTION_AUTO);
			String messageId, transactionId, sourceId, targetAppID, messageTimestamp, serviceName, operationName, serviceVersion, hostName, clientIP, status, additionalInfo, transactionDetail, timeduration, responseStatus, errorCode, errorMessage;
			messageId = transactionId = sourceId = targetAppID = messageTimestamp = serviceName = operationName = serviceVersion = hostName = clientIP = status = additionalInfo = transactionDetail = timeduration = responseStatus = errorCode = errorMessage = null;
			messageId = getValue(body, "DataLog/MessageId");
			transactionId = getValue(body, "DataLog/TransactionId");
			sourceId = getValue(body, "DataLog/SourceId");
			targetAppID = getValue(body, "DataLog/TargetId");
			messageTimestamp = getValue(body, "DataLog/MessageTimestamp");
			serviceName = getValue(body, "DataLog/ServiceName");
			operationName = getValue(body, "DataLog/OperationName");
			serviceVersion = getValue(body, "DataLog/ServiceVersion");
			hostName = getValue(body, "DataLog/HostName");
			clientIP = getValue(body, "DataLog/ClientIP");
			status = getValue(body, "DataLog/Status");
			additionalInfo = getValue(body, "DataLog/AdditionalInfo");
			transactionDetail = getValue(body, "DataLog/TransactionDetail");

			if (status != null) {
				if (status.equals("Response")) {
				timeduration = getValue(body, "DataLog/Timeduration");
				responseStatus = getValue(body, "DataLog/ResponseStatus");
				errorCode = getValue(body, "DataLog/ErrorCode");
				errorMessage = getValue(body, "DataLog/ErrorMessage");
				}
			};

			cs = conn.prepareCall("{call pkg_soaprocesslog.create_log_soalog(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
			cs.setString(1, messageId);
			cs.setString(2, transactionId);
			cs.setString(3, sourceId);
			cs.setString(4, targetAppID);
			//Timestamp toTimestamp = Timestamp.valueOf(messageTimestamp);
			//String timestamp = "2019-08-27T16:03:16.75";
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
			if (messageTimestamp != null) {
				if (messageTimestamp.length() == 19){
				messageTimestamp += ".000";
				}
			}
			Date dateFromString = dateFormat.parse(messageTimestamp);
			Timestamp timeStamp = new Timestamp(dateFromString.getTime());
			cs.setTimestamp(5, timeStamp);
			cs.setString(6, serviceName);
			cs.setString(7, operationName);
			cs.setString(8, serviceVersion);
			cs.setString(9, timeduration);
			cs.setString(10, status);
			cs.setObject(11, additionalInfo);
			cs.setObject(12, transactionDetail);
			cs.setString(13, responseStatus);
			cs.setString(14, errorCode);
			cs.setString(15, errorMessage);
			cs.setString(16, hostName);
			cs.setString(17, clientIP);
			cs.execute();
			cs.close();
		
			// End of user code
			// ----------------------------------------------------------
		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be
			// handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		} finally {
			try {
				if (cs != null)
					cs.close();
			} catch (SQLException se2) {
				//se2.printStackTrace();
			}
		}

		// Change following to propagate the message to the 'out' or 'alt'
		// terminal
		//out.propagate(assembly);
	}

//	private void If(boolean equals) {
//		// TODO Auto-generated method stub
//
//	}

	private String getValue(MbElement body, String path) {
		String result = null;
		try {
			if (body.getFirstElementByPath(path) != null) {
				result = body.getFirstElementByPath(path).getValueAsString();
			}
		} catch (MbException e) {
			// TODO Auto-generated catch block
			return null;
		}
		return result;
	}
}
