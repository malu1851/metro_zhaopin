package nc.util.rm;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.buzimsg.util.BuziMsgSendingUtil;
import nc.buzimsg.vo.BuziMsgSendingContext;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.itf.uap.sf.IServiceProviderSerivce;
import nc.mail.MailSender;
import nc.message.config.MailConfigAccessor;
import nc.message.vo.MessageVO;
import nc.message.vo.NCMessage;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.hr.message.HRBusiMessageVO;
import nc.vo.ml.LanguageVO;
import nc.vo.ml.MultiLangContext;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.sm.UserVO;

/***
 * �����ʼ�������
 * @author ������
 *
 */
public class AttachFileUtils {
	
	  private String mailHost = null;
	  private String username = null;
	  private String password = null;
	  private boolean isAuthen = true;
	
	 public AttachFileUtils() {
		
	}

	private MailSender sender = null;
	
	
	 public void sendBuziMessage_RequiresNew(HRBusiMessageVO messageInfoVO,List<String> filePaths) throws Exception
	 {		 
		 sendNCMessage(messageInfoVO, new String[] { getUserContentLangCode(PubEnv.getPk_user())},filePaths);		 
	  }
	 
	 public void sendNCMessage(HRBusiMessageVO messageInfoVO, String[] langcodes,List<String> filePaths)
			    throws BusinessException, MessagingException
			  {		     
			    String[] langcodeArray = langcodes;			    
			    if (ArrayUtils.isEmpty(langcodeArray))
			    {
			      String langCode = InvocationInfoProxy.getInstance().getLangCode();
			      langcodeArray = new String[] { langCode };
			    }		    
			    BuziMsgSendingContext context = new BuziMsgSendingContext();			    
			    NCMessage ncMessage = new NCMessage();			    
			    buildNCMessage(messageInfoVO, ncMessage, context);			    
			    Map<String, NCMessage> messageMap = BuziMsgSendingUtil.sendBuziMsgWithReturn(ncMessage, context, langcodeArray);			    
			    for (int i = 0; i < langcodeArray.length; i++)
			    {
			      NCMessage message = (NCMessage)messageMap.get(langcodeArray[i]);
			      //sendEmail(message, messageInfoVO, filePaths);
			      if (message != null)
			      {
			        String msgType = message.getMessage().getMsgtype();			        
			        if (msgType.indexOf("email") > -1)
			        {
			          if (!ArrayUtils.isEmpty(messageInfoVO.getReceiverEmails()))
			          {		            
			            try
			            {		   
			            	  sendEmail(message, messageInfoVO, filePaths);			              	              
			            }
			            catch (SendFailedException ex)
			            {
			              Address[] invalidAddress = ex.getInvalidAddresses();
			              Address[] unsentAddress = ex.getValidUnsentAddresses();
			              String errorMsg = ResHelper.getString("6001msgtmp", "06001msgtmp0046");			              
			              String failEmailAddress = "";
			              if (!ArrayUtils.isEmpty(invalidAddress)) {
			                for (Address address : invalidAddress)
			                {
			                  if (StringUtils.isEmpty(failEmailAddress))
			                  {
			                    failEmailAddress = address.toString();
			                  }
			                  else
			                  {
			                    failEmailAddress = failEmailAddress + "," + address.toString();
			                  }
			                }
			              }
			              
			              if (!ArrayUtils.isEmpty(unsentAddress)) {
			                for (Address address : unsentAddress)
			                {
			                  if (StringUtils.isEmpty(failEmailAddress))
			                  {
			                    failEmailAddress = address.toString();
			                  }
			                  else
			                  {
			                    failEmailAddress = failEmailAddress + "," + address.toString();
			                  }
			                }
			              }			              
			              if (StringUtils.isNotEmpty(failEmailAddress))
			              {
			                errorMsg = errorMsg + ResHelper.getString("6001msgtmp", "06001msgtmp0047", new String[] { failEmailAddress });
			              }			              
			              throw new BusinessException(errorMsg, ex);
			            }
			            catch (Exception ex)
			            {
			              throw new BusinessException(ex.getMessage(), ex);
			            }
			          }
			        }
			    }
			      }
			   
     }
	 
	 /***
	  * ���ʹ������ʼ�
	  * ������
	  * @return
	 * @throws BusinessException 
	 * @throws MessagingException 
	 * @throws UnsupportedEncodingException 
	  * @throws Exception
	  */
	 public void sendEmail(NCMessage message,HRBusiMessageVO messageInfoVO,List<String> filePaths) throws BusinessException, MessagingException, UnsupportedEncodingException 
	  {
	    
	      this.mailHost= MailConfigAccessor.getSMTPServer();
	      this.username = MailConfigAccessor.getUser();
	      this.password = MailConfigAccessor.getPassword();
	      this.isAuthen= MailConfigAccessor.isAuthen();
	      boolean bAuth = MailConfigAccessor.isAuthen();
	      if (StringUtils.isEmpty(mailHost)) {
	        throw new BusinessException(ResHelper.getString("6001msgtmp", "16001msgtmp0005"));
	      }	      
	      if (StringUtils.isEmpty(username)) {
	        throw new BusinessException(ResHelper.getString("6001msgtmp", "16001msgtmp0006"));
	      }
	     // ����Properties����  
	      Properties props = new Properties();
	     // �����ż�������
	      props.put("mail.smtp.host",mailHost);
	      props.put("mail.smtp.auth", "true"); // ͨ����֤  
	        // �õ�Ĭ�ϵĶԻ�����  
	        Session session = Session.getDefaultInstance(props, null); 
	                       
	        // ����һ����Ϣ������ʼ������Ϣ�ĸ���Ԫ��  
	        MimeMessage msg = new MimeMessage(session); 
	        //�����˵�ַ
	        String	from  = MailConfigAccessor.getMailFrom();
		    //�ռ��˵�ַ           
		    String[]  to  = messageInfoVO.getReceiverEmails();
		    // �����BodyPart�����뵽�˴�������Multipart��  
            Multipart mp = new MimeMultipart();
            //���ñ���
            msg.setSubject(message.getMessage().getSubject());
            //��������
            MimeBodyPart mbpContent = new MimeBodyPart();
            mbpContent.setText(message.getMessage().getContent());
            mp.addBodyPart(mbpContent);
            // ��������  
            if (filePaths != null && filePaths.size() > 0) {  
                for (String filename : filePaths) {  
                    MimeBodyPart mbpAttach = new MimeBodyPart();  
                    // �õ�����Դ  
                    FileDataSource fds = new FileDataSource(filename);  
                    // �õ�������������BodyPart                
					mbpAttach.setDataHandler(new DataHandler(fds));					 
                    // �õ��ļ���ͬ������BodyPart  
					filename= new String(fds.getName().getBytes(),"ISO-8859-1");
                    mbpAttach.setFileName(filename);  
                    mp.addBodyPart(mbpAttach);  
                }       
                              
            } 
            // ���߼����е�����Ԫ��  
            filePaths =null;    
            // Multipart���뵽�ż�  
            msg.setContent(mp); 
            //���÷�����
            if ((from != null) && (from.trim().length() > 0)) {
                msg.setFrom(new InternetAddress(from));
            }
            //�����ռ���
            msg.setRecipients(Message.RecipientType.TO, convertToAddress(to));
            // �����ż�ͷ�ķ�������  
            msg.setSentDate(new Date());  
            msg.saveChanges();  
            // �����ż�  
            Transport transport = session.getTransport("smtp");  
            transport.connect(this.mailHost, this.username, this.password);      
            transport.sendMessage(msg,  
                    msg.getRecipients(Message.RecipientType.TO));  
            transport.close();  
	        
	 }	
	 	 
	 
	 private String getUserContentLangCode(String userPk) throws Exception {
		    UserVO[] users = ((IUserPubService)NCLocator.getInstance().lookup(IUserPubService.class)).getUsersByPKs(new String[] { userPk });
		    if (ArrayUtils.isEmpty(users))
		      return null;
		    UserVO user = users[0];
		    String pk_langcode = user.getContentlang();
		    LanguageVO[] all = getAllEnabledLangVO();
		    if (ArrayUtils.isEmpty(all))
		      return null;
		    for (LanguageVO languageVO : all) {
		      if (languageVO.getPk_multilang().equals(pk_langcode))
		        return languageVO.getLangcode();
		    }
		    return null;
		  }
	 
	 private static LanguageVO[] getAllEnabledLangVO()
	  {
	    LanguageVO[] langvos = MultiLangContext.getInstance().getEnableLangVOs();
	    if (ArrayUtils.isEmpty(langvos))
	      return null;
	    return langvos;
	  }
	 
	 
	  private void buildNCMessage(HRBusiMessageVO messageInfoVO, NCMessage ncMessage, BuziMsgSendingContext context)
			    throws BusinessException
			  {
			    if (ncMessage == null)
			    {
			      ncMessage = new NCMessage();
			    }
			    if (context == null)
			    {
			      context = new BuziMsgSendingContext();
			    }
			    
			    context.setBillVO(messageInfoVO.getBillVO());
			    context.setMsgrescode(messageInfoVO.getMsgrescode());
			    context.setPkorgs(messageInfoVO.getPkorgs());
			    

			    MessageVO messageVO = new MessageVO();
			    
			    messageVO.setMsgsourcetype("notice");
			    messageVO.setContenttype("text/html");
			    
			    StringBuffer strReceiver = new StringBuffer();
			    if (!ArrayUtils.isEmpty(messageInfoVO.getReceiverPkUsers()))
			    {
			      for (String userId : messageInfoVO.getReceiverPkUsers())
			      {
			        if (strReceiver.indexOf(userId) < 0)
			        {


			          if (strReceiver.length() == 0)
			          {
			            strReceiver.append(userId);
			          }
			          else
			          {
			            strReceiver.append(",").append(userId); }
			        }
			      }
			    }
			    messageVO.setReceiver(strReceiver.toString());
			    

			    messageVO.setIsdelete(UFBoolean.FALSE);
			    if (StringUtils.isNotBlank(messageInfoVO.getSender()))
			    {
			      messageVO.setSender(messageInfoVO.getSender());

			    }
			    else
			    {
			      String pk_user = PubEnv.getPk_user();
			      if ((pk_user == null) || ("#UAP#".equals(pk_user)))
			      {
			        pk_user = "NC_USER0000000000000";
			      }
			      messageVO.setSender(pk_user);
			    }
			    if (messageInfoVO.getSendtime() != null)
			    {
			      messageVO.setSendtime(messageInfoVO.getSendtime());

			    }
			    else
			    {
			      IServiceProviderSerivce srvProvinder = (IServiceProviderSerivce)NCLocator.getInstance().lookup(IServiceProviderSerivce.class);
			      messageVO.setSendtime(srvProvinder.getServerTime());
			    }
			    messageVO.setDr(Integer.valueOf(0));
			    ncMessage.setMessage(messageVO);
			  }
	  
	  
	  
	  //����һ��������֤��
	  private class NCMailAuthenticator extends Authenticator
	  {
	    private NCMailAuthenticator() {}
	    
	    protected PasswordAuthentication getPasswordAuthentication() {
	      return new PasswordAuthentication(AttachFileUtils.this.username, AttachFileUtils.this.password);
	    }
	  }
	  //���ַ���ת�����ʼ���ַ
	  private Address[] convertToAddress(String[] strs) throws AddressException {
		    int count = strs == null ? 0 : strs.length;
		    Address[] address = new Address[count];
		    for (int i = 0; i < count; i++) {
		      address[i] = new InternetAddress(strs[i]);
		    }
		    return address;
		  }

}





