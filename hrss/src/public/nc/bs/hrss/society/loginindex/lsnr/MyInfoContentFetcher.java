/*    */ package nc.bs.hrss.society.loginindex.lsnr;
/*    */ 
/*    */ import nc.bs.framework.common.NCLocator;
import nc.bs.hrss.RmManageService.WebRmManageService;
import nc.bs.hrss.pub.exception.HrssException;
/*    */ import nc.itf.rm.IRMPsndocQueryService;
/*    */ import nc.uap.lfw.core.page.LfwView;
/*    */ import nc.uap.lfw.core.page.LfwWindow;
/*    */ import nc.uap.lfw.jsp.uimeta.UIMeta;
import nc.ui.ic.pub.util.CardPanelWrapper;
/*    */ import nc.vo.hrss.pub.rmweb.RmUserVO;
/*    */ import nc.vo.ml.AbstractNCLangRes;
/*    */ import nc.vo.ml.NCLangRes4VoTransl;
/*    */ import nc.vo.pub.BusinessException;
/*    */ import nc.vo.rm.psndoc.AggRMPsndocVO;
/*    */ import nc.vo.rm.psndoc.RMPsndocVO;
/*    */ 
/*    */ public class MyInfoContentFetcher implements nc.uap.lfw.core.comp.IWebPartContentFetcher
/*    */ {
/*    */   public MyInfoContentFetcher() {}
/*    */   
/*    */   public String fetchHtml(UIMeta um, LfwWindow pm, LfwView view)
/*    */   {
/* 21 */     nc.vo.hrss.pub.SessionBean session = nc.bs.hrss.pub.tool.SessionUtil.getRMWebSessionBean();
/* 22 */     RmUserVO rmUserVO = session.getRmUserVO();
/* 23 */     String name = "";
/* 24 */     String mobile = "";
/* 25 */     String email = "";
/* 26 */     String idcard = "";
             String statu =null;
             //获取个人主键
             WebRmManageService service = NCLocator.getInstance().lookup(
	              WebRmManageService.class);
           
/*    */     try {
/* 28 */       AggRMPsndocVO aggRMPsndocVO = ((IRMPsndocQueryService)nc.bs.hrss.pub.ServiceLocator.lookup(IRMPsndocQueryService.class)).queryByPK(rmUserVO.getHrrmpsndoc());
/* 29 */       if (aggRMPsndocVO != null) {
/* 30 */         name = aggRMPsndocVO.getPsndocVO().getMultilangName();
/* 31 */         mobile = aggRMPsndocVO.getPsndocVO().getMobile();
/* 32 */         email = aggRMPsndocVO.getPsndocVO().getEmail();
/* 33 */         idcard = aggRMPsndocVO.getPsndocVO().getId();
                 String  psndoc= aggRMPsndocVO.getPsndocVO().getPk_psndoc();
                 Object[] RmStatu = service.RmStatu(psndoc);
                 
                 if (RmStatu ==null){
                	 
                	 statu = "未申请岗位";
                	 
                 }
                 else{
                
                 if (Integer.parseInt(RmStatu[0].toString()) == 0){
                	 
                	 //statu = "初始态";
                	 statu = "未申请岗位";
         
                    }
                 else if (Integer.parseInt(RmStatu[0].toString()) == 1){
                	 
                	 statu = "应聘登记";
         
                    }
                 else if (Integer.parseInt(RmStatu[0].toString()) == 2){
     	 
     	             statu = "初选通过";

                    }
                 else if (Integer.parseInt(RmStatu[0].toString()) == 3){
     	 
     	             statu = "面试中";

                    }
                 else if (Integer.parseInt(RmStatu[0].toString()) == 4){
     	 
     	             statu = "录用中";

                    }
                 else  if (Integer.parseInt(RmStatu[0].toString()) == 5){
     	 
     	             statu = "报道中";

                    }
                 else  if (Integer.parseInt(RmStatu[0].toString()) == 6){
                 	 
     	             statu = "已入职";

                    }
/*    */          } 
                 
                }else {
/* 35 */         if (org.apache.commons.lang.StringUtils.isEmpty(name))
/*    */         {
/* 37 */           name = rmUserVO.getName();
/*    */         }
/* 39 */         email = rmUserVO.getEmail();
/* 40 */         idcard = rmUserVO.getIdcard();
                String  psndoc= rmUserVO.getHrrmpsndoc();
				Object[] RmStatu = service.RmStatu(psndoc);
				
				if(RmStatu ==null){
					
					statu = "未申请职位";
				}
				else{
				
				if (Integer.parseInt(RmStatu[0].toString()) == 0){
					 
					 statu = "初始态";
				
				   }
				else if (Integer.parseInt(RmStatu[0].toString()) == 1){
					 
					 statu = "应聘登记";
				
				   }
				else if (Integer.parseInt(RmStatu[0].toString()) == 2){
				
				     statu = "初选通过";
				
				   }
				else if (Integer.parseInt(RmStatu[0].toString()) == 3){
				
				     statu = "面试中";
				
				   }
				else if (Integer.parseInt(RmStatu[0].toString()) == 4){
				
				     statu = "录用中";
				
				   }
				else  if (Integer.parseInt(RmStatu[0].toString()) == 5){
				
				     statu = "报道中";
				
				   }
				else  if (Integer.parseInt(RmStatu[0].toString()) == 6){
					 
				     statu = "已入职";
				
				   }
/*    */       }
              }
/*    */     } catch (BusinessException e) {
/* 43 */       new HrssException(e).deal();
/*    */     } catch (HrssException e) {
/* 45 */       e.alert();
/*    */     }
/* 47 */     StringBuffer buf = new StringBuffer("<div id=\"nameinfo\" style=\"border:0px ;position:relative;margin-top:20px;size:20px;padding-left:10px;line-height:25px;\">");
/* 48 */     buf.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0032") + (name == null ? "" : name));
/* 49 */     buf.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0033") + (mobile == null ? "" : mobile));
/* 50 */     buf.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0034") + (email == null ? "" : email));
/* 51 */     buf.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0035") + (idcard == null ? "" : idcard));
			 buf.append("<div style=\"color:red;\">");
			 buf.append("应聘状态：" );
             buf.append(  statu);
             buf.append("</div>");
/* 52 */     buf.append("</div>");
/* 53 */     return buf.toString();
/*    */   }
/*    */   
/*    */   public String fetchBodyScript(UIMeta um, LfwWindow pm, LfwView view)
/*    */   {
/* 58 */     return "";
/*    */   }
/*    */ }

/* Location:           E:\Metro_NC_HOME\NC633GOLD\modules\hrss\classes
 * Qualified Name:     nc.bs.hrss.society.loginindex.lsnr.MyInfoContentFetcher
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.0.1
 */