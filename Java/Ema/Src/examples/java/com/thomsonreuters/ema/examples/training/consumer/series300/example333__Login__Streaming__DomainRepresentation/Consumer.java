///*|----------------------------------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      	--
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  					--
// *|           Copyright Thomson Reuters 2017. All rights reserved.            		--
///*|----------------------------------------------------------------------------------------------------

package com.thomsonreuters.ema.examples.training.consumer.series300.example333__Login__Streaming__DomainRepresentation;

import com.thomsonreuters.ema.access.AckMsg;
import com.thomsonreuters.ema.access.Data;
import com.thomsonreuters.ema.access.DataType;
import com.thomsonreuters.ema.access.DataType.DataTypes;
import com.thomsonreuters.ema.access.EmaFactory;
import com.thomsonreuters.ema.access.FieldEntry;
import com.thomsonreuters.ema.access.FieldList;
import com.thomsonreuters.ema.access.GenericMsg;
import com.thomsonreuters.ema.access.Msg;
import com.thomsonreuters.ema.access.OmmConsumer;
import com.thomsonreuters.ema.access.OmmConsumerClient;
import com.thomsonreuters.ema.access.OmmConsumerEvent;
import com.thomsonreuters.ema.access.OmmException;
import com.thomsonreuters.ema.access.RefreshMsg;
import com.thomsonreuters.ema.access.ReqMsg;
import com.thomsonreuters.ema.access.StatusMsg;
import com.thomsonreuters.ema.access.UpdateMsg;
import com.thomsonreuters.ema.domain.login.Login.LoginRefresh;
import com.thomsonreuters.ema.domain.login.Login.LoginReq;
import com.thomsonreuters.ema.domain.login.Login.LoginStatus;
import com.thomsonreuters.ema.rdm.EmaRdm;


class AppClient implements OmmConsumerClient
{
    LoginRefresh _loginRefresh = EmaFactory.Domain.createLoginRefresh();
    LoginStatus _loginStatus = EmaFactory.Domain.createLoginStatus();
    
	public void onRefreshMsg(RefreshMsg refreshMsg, OmmConsumerEvent event)
	{
		System.out.println("Received Refresh. Item Handle: " + event.handle() + " Closure: " + event.closure());
		
		System.out.println("Item Name: " + (refreshMsg.hasName() ? refreshMsg.name() : "<not set>"));
		System.out.println("Service Name: " + (refreshMsg.hasServiceName() ? refreshMsg.serviceName() : "<not set>"));

		System.out.println("Item State: " + refreshMsg.state());

		if (refreshMsg.domainType() == EmaRdm.MMT_LOGIN)
		{   
		    _loginRefresh.clear();
	        System.out.println(_loginRefresh.message(refreshMsg).toString());   
		}
		else
	    {
		    decode( refreshMsg );
	    }
			
		System.out.println();
	}
	
	public void onUpdateMsg(UpdateMsg updateMsg, OmmConsumerEvent event) 
	{
		System.out.println("Received Update. Item Handle: " + event.handle() + " Closure: " + event.closure());
		
		System.out.println("Item Name: " + (updateMsg.hasName() ? updateMsg.name() : "<not set>"));
		System.out.println("Service Name: " + (updateMsg.hasServiceName() ? updateMsg.serviceName() : "<not set>"));
		
		decode( updateMsg );
		
		System.out.println();
	}

	public void onStatusMsg(StatusMsg statusMsg, OmmConsumerEvent event) 
	{
		System.out.println("Received Status. Item Handle: " + event.handle() + " Closure: " + event.closure());

		System.out.println("Item Name: " + (statusMsg.hasName() ? statusMsg.name() : "<not set>"));
		System.out.println("Service Name: " + (statusMsg.hasServiceName() ? statusMsg.serviceName() : "<not set>"));

		if (statusMsg.hasState())
			System.out.println("Item State: " +statusMsg.state());
		
        if (statusMsg.domainType() == EmaRdm.MMT_LOGIN)
        {
            _loginStatus.clear();
            System.out.println(_loginStatus.message(statusMsg).toString());   
        }
		
		System.out.println();
	}
	
	public void onAckMsg(AckMsg ackMsg, OmmConsumerEvent event) {}
	public void onGenericMsg(GenericMsg genericMsg, OmmConsumerEvent event){}
	public void onAllMsg(Msg msg, OmmConsumerEvent event){}

	void decode( Msg msg )
	{
	    if (msg.attrib().dataType() == DataTypes.FIELD_LIST)
	        decode(msg.attrib().fieldList());
	    
	    if (msg.payload().dataType() == DataTypes.FIELD_LIST)
	        decode(msg.payload().fieldList());
	}
	
	void decode(FieldList fieldList)
	{
		for (FieldEntry fieldEntry : fieldList)
		{
			System.out.print("Fid: " + fieldEntry.fieldId() + " Name = " + fieldEntry.name() + " DataType: " + DataType.asString(fieldEntry.load().dataType()) + " Value: ");

			if (Data.DataCode.BLANK == fieldEntry.code())
				System.out.println(" blank");
			else
				switch (fieldEntry.loadType())
				{
				case DataTypes.REAL :
					System.out.println(fieldEntry.real().asDouble());
					break;
				case DataTypes.DATE :
					System.out.println(fieldEntry.date().day() + " / " + fieldEntry.date().month() + " / " + fieldEntry.date().year());
					break;
				case DataTypes.TIME :
					System.out.println(fieldEntry.time().hour() + ":" + fieldEntry.time().minute() + ":" + fieldEntry.time().second() + ":" + fieldEntry.time().millisecond());
					break;
				case DataTypes.INT :
					System.out.println(fieldEntry.intValue());
					break;
				case DataTypes.UINT :
					System.out.println(fieldEntry.uintValue());
					break;
				case DataTypes.ASCII :
					System.out.println(fieldEntry.ascii());
					break;
				case DataTypes.ENUM :
					System.out.println(fieldEntry.enumValue());
					break;
				case DataTypes.RMTES:
				    System.out.println(fieldEntry.rmtes());
				    break;
				case DataTypes.ERROR :
					System.out.println(fieldEntry.error().errorCode() +" (" + fieldEntry.error().errorCodeAsString() + ")");
					break;
				default :
					System.out.println();
					break;
				}
		}
	}
}

public class Consumer 
{
	public static void main(String[] args)
	{
		OmmConsumer consumer = null;
		try
		{
			AppClient appClient = new AppClient();
			
			consumer  = EmaFactory.createOmmConsumer(EmaFactory.createOmmConsumerConfig().host("localhost:14002").username("user"));
		
			LoginReq loginReq = EmaFactory.Domain.createLoginReq();
			
			long loginHandle = consumer.registerClient(loginReq.message(), appClient);
			
	        ReqMsg reqMsg = EmaFactory.createReqMsg();
			
			long handle = consumer.registerClient(reqMsg.serviceName("DIRECT_FEED").name("IBM.N"), appClient);

			Thread.sleep(60000);			// API calls onRefreshMsg(), onUpdateMsg() and onStatusMsg()
		} 
		catch (InterruptedException | OmmException excp)
		{
			System.out.println(excp.getMessage());
		}
		finally 
		{
			if (consumer != null) consumer.uninitialize();
		}
	}
}
