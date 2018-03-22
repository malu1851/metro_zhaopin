package nc.bs.hrss.society.loginindex.lsnr;

import nc.bs.framework.common.NCLocator;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.hrss.pub.tool.SessionUtil;
import nc.itf.hr.frame.IPersistenceRetrieve;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.LfwWindow;
import nc.uap.lfw.jsp.uimeta.UIMeta;
import nc.vo.hrss.pub.SessionBean;
import nc.vo.hrss.pub.rmweb.RmUserVO;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.rm.job.RMJobVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.psndoc.common.RMApplyStatusEnum;
import nc.vo.rm.publish.PublishJobVO;
import org.apache.commons.lang.StringUtils;

public class LogoutContentFetcher implements nc.uap.lfw.core.comp.IWebPartContentFetcher
{
  public LogoutContentFetcher() {}
  
  public String fetchHtml(UIMeta um, LfwWindow pm, LfwView view)
  {
    StringBuffer buf = new StringBuffer("<div id=\"toEntryGuide\" style=\"border:0px solid red;position:relative;margin-top:40px;padding-left:10px;line-height:15px;\">");
//    String jobName = getMultilangName(getJobName());
//    String s0 = NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0044");
//    if (!StringUtils.isEmpty(jobName)) {
//      String s1 = NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0045") + "<b>" + jobName + "</b>" + NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0046");
//      
//
//      String s2 = NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0043");
//      buf.append(s0 + "<br>" + s1);
//      buf.append("<a href=\"entryGuide.html\" target=\"parentWindow\" style=\"color:blue;\">" + s2 + "</a>");
//    }
    buf.append("</div> <br>");
    buf.append("<div id=\"logout\" style=\"border:0px solid red;position:relative;margin-top:40px;padding-left:0px;line-height:15px;\">");
    buf.append("<ul>");
    SessionBean bean = SessionUtil.getRMWebUnLoginSessionBean();
    
    String s3 = NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0030");
    String s4 = NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0031");
    
    if (bean.getBrower_jobtyle() == 2) {
      buf.append("<li><a href=\"javascript:toActivePage();\" >" + s3 + "</a></li>");
    }
    buf.append("<li><a href=\"javascript:logout();\">" + s4 + "</a></li>");
    buf.append("</ul>");
    buf.append("</div>");
    return buf.toString();
  }
  
  public String fetchBodyScript(UIMeta um, LfwWindow pm, LfwView view)
  {
    return "";
  }
  





  private RMJobVO getJobName()
  {
    SessionBean bean = SessionUtil.getRMWebSessionBean();
    if (bean == null) {
      return null;
    }
    RmUserVO rmUserVO = bean.getRmUserVO();
    String pk_psndoc = rmUserVO.getHrrmpsndoc();
    if (StringUtils.isEmpty(pk_psndoc)) {
      return null;
    }
    

    AggRMPsndocVO aggVO = nc.bs.hrss.postApply.schl.ctrl.SchlRmPostViewList.getAggRMPsndocVO(pk_psndoc);
    if (aggVO == null) {
      return null;
    }
    RMPsnJobVO[] rmPsnJobVO = (RMPsnJobVO[])aggVO.getTableVO(RMPsnJobVO.getDefaultTableName());
    if (rmPsnJobVO == null) {
      return null;
    }
    Integer applystatus = null;
    

    String pk_reg_job = "";
    for (RMPsnJobVO vo : rmPsnJobVO) {
      applystatus = vo.getApplystatus();
      if (applystatus.intValue() >= RMApplyStatusEnum.HIRE.getReturnType()) {
        pk_reg_job = vo.getPk_reg_job();
        break;
      }
    }
    if (StringUtils.isEmpty(pk_reg_job)) {
      return null;
    }
    
    PublishJobVO publishJobVO = null;
    RMJobVO rmJobVO = null;
    IPersistenceRetrieve retrieve = (IPersistenceRetrieve)NCLocator.getInstance().lookup(IPersistenceRetrieve.class);
    try {
      publishJobVO = (PublishJobVO)retrieve.retrieveByPk(PublishJobVO.class, pk_reg_job, null);
      if (publishJobVO == null) {
        return null;
      }
      if (StringUtils.isEmpty(publishJobVO.getPk_job())) {
        return null;
      }
      rmJobVO = (RMJobVO)retrieve.retrieveByPk(RMJobVO.class, publishJobVO.getPk_job(), null);
    } catch (BusinessException e) {
      new HrssException(e).deal();
    }
    return rmJobVO;
  }
  






  public String getMultilangName(RMJobVO jobVO)
  {
    if (jobVO == null) {
      return null;
    }
    return nc.hr.utils.MultiLangHelper.getName(jobVO);
  }
}
