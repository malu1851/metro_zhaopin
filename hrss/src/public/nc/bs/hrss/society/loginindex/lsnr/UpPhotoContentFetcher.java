/*    */ package nc.bs.hrss.society.loginindex.lsnr;
/*    */ 
/*    */ import nc.uap.lfw.core.comp.IWebPartContentFetcher;
/*    */ import nc.uap.lfw.core.page.LfwView;
/*    */ import nc.uap.lfw.core.page.LfwWindow;
/*    */ import nc.uap.lfw.jsp.uimeta.UIMeta;
/*    */ import nc.vo.ml.AbstractNCLangRes;
/*    */ import nc.vo.ml.NCLangRes4VoTransl;
/*    */ 
/*    */ public class UpPhotoContentFetcher
/*    */   implements IWebPartContentFetcher
/*    */ {
/*    */   public UpPhotoContentFetcher() {}
/*    */   
/*    */   public String fetchHtml(UIMeta um, LfwWindow pm, LfwView view)
/*    */   {
/* 11 */     String s1 = NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0036");
/* 12 */     String s2 = NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0037");
/* 13 */     String s3 = NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0026");
/* 14 */     StringBuffer buf = new StringBuffer("<div id=\"upphoto\" style=\"border:0px solid red;position:relative;margin-top:30px;padding-left:0px;line-height:15px;\">");
/* 15 */     buf.append("<ul>");
/* 16 */     buf.append("<li><a style =\"font-size:14px\" href=\"javascript:updphoto();\">" + s1 + "</a></li>");
/* 17 */     buf.append("<li><a style =\"font-size:14px\" href=\"javascript:updresume();\">" + "ÐÞ¸Ä¼òÀú" + "</a></li>");
/* 18 */     buf.append("<li><a style =\"font-size:14px\" href=\"javascript:updpassword();\">" + s3 + "</a></li>");
/* 19 */     buf.append("</ul>");
/* 20 */     buf.append("</div>");
/* 21 */     return buf.toString();
/*    */   }
/*    */   
/*    */   public String fetchBodyScript(UIMeta um, LfwWindow pm, LfwView view)
/*    */   {
/* 26 */     return "";
/*    */   }
/*    */ }

/* Location:           E:\ftp\NC633GOLD20170725\modules\hrss\classes
 * Qualified Name:     nc.bs.hrss.society.loginindex.lsnr.UpPhotoContentFetcher
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.7.0.1
 */