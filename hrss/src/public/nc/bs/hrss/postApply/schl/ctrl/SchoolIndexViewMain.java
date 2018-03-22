package nc.bs.hrss.postApply.schl.ctrl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.hrss.pub.ServiceLocator;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.hrss.pub.tool.CommonUtil;
import nc.bs.hrss.pub.tool.DatasetUtil;
import nc.bs.hrss.pub.tool.SessionUtil;
import nc.itf.rm.IActiveQueryService;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.ArrayProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.comp.WebComponent;
import nc.uap.lfw.core.ctrl.IController;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.ctx.ViewContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.PaginationInfo;
import nc.uap.lfw.core.data.RowSet;
import nc.uap.lfw.core.event.DataLoadEvent;
import nc.uap.lfw.core.event.LinkEvent;
import nc.uap.lfw.core.event.ScriptEvent;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.ViewComponents;
import nc.uap.lfw.core.serializer.impl.SuperVO2DatasetSerializer;
import nc.ui.trade.business.HYPubBO_Client;
import nc.vo.hrss.pub.SessionBean;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.rm.active.ActiveVO;
import nc.vo.rm.active.AggActiveVO;
import org.apache.commons.lang.ArrayUtils;
import uap.web.bd.pub.AppUtil;

public class SchoolIndexViewMain
  implements IController
{
  private static final long serialVersionUID = 1L;
  public static final String DS_SCHLRMACTVTY = "dsSchlRmActvty";
  
  public SchoolIndexViewMain() {}
  
  public void onDataLoad_dsSchlRmActvty(DataLoadEvent dataLoadEvent)
  {
    LfwView widget = AppLifeCycleContext.current().getViewContext().getView();
    	
    if (SessionUtil.getRMWebSessionBean() != null) {
      widget.getViewComponents().getComponent("lnkRegist").setVisible(false);
      widget.getViewComponents().getComponent("lnkRmWebLogin").setVisible(false);
    } else {
      widget.getViewComponents().getComponent("lnkRegist").setVisible(true);
      widget.getViewComponents().getComponent("lnkRmWebLogin").setVisible(true);
    }
    
    Dataset ds = (Dataset)dataLoadEvent.getSource();
    if (!isPagination(ds)) {
      DatasetUtil.clearData(ds);
    }
    IActiveQueryService activeQueryService = null;
    AggActiveVO[] aggVOs = null;
    
    List<SuperVO> superVOlist = new ArrayList();
    
    /**
     * 柳衍志-招聘活动VO列表
     */
    List<ActiveVO> activeVoList = new ArrayList<ActiveVO>();
    try {
      activeQueryService = (IActiveQueryService)ServiceLocator.lookup(IActiveQueryService.class);
      aggVOs = activeQueryService.queryByCondition("activestate = 1");

  	IUAPQueryBS iUAPQueryBS = (IUAPQueryBS) NCLocator.getInstance().lookup(
  			IUAPQueryBS.class.getName());
  	
  	//查询发布的所有的招聘活动
  	StringBuffer sb = new StringBuffer();
  	sb.append("select * from rm_active where activestate = 1 ");
  	
  	activeVoList = (List<ActiveVO>) iUAPQueryBS.executeQuery(sb.toString(),new BeanListProcessor(ActiveVO.class));
   
    } catch (HrssException e){
      e.alert();
    } catch (BusinessException e) {
      new HrssException(e).deal();
    }
    //!ArrayUtils.isEmpty(aggVOs
    if (activeVoList.isEmpty()==false) {
		
		SessionBean unLoginBean = SessionUtil.getRMWebUnLoginSessionBean();
		//获取前台jsp界面的type来区分校招和社招，type=1 社招，校招
		int  interviewtype = unLoginBean.getType();
		
		if(interviewtype ==0){
			
			CommonUtil.showMessageDialog("获取界面数据数据异常");	
		}
		for (ActiveVO activevo :activeVoList) {
			//社会招聘
			if (interviewtype==1){
			String pk_active = (String) activevo.getPk_active();
			StringBuffer strBuffer = new StringBuffer();
			strBuffer.append("select count(*)");
			strBuffer.append("from  rm_active  a  inner  join rm_publish  b");
			strBuffer.append(" on  a.pk_active = b.pk_activity");
			strBuffer.append(" left join rm_publishplace c");
			strBuffer.append(" on  b.pk_publishjob =  c.pk_publishjob");
			strBuffer.append(" where  c.place = 1 and a.pk_active ='"+pk_active+"'");
			try {
				BaseDAO dao = new BaseDAO();
				Object[] count = (Object[]) dao.executeQuery(
						strBuffer.toString(), new ArrayProcessor());
				 if(Integer.parseInt(count[0].toString()) > 0 ){
						superVOlist.add((SuperVO) activevo);
				 }
			} catch (BusinessException e) {
				e.printStackTrace();
			}
			//校园招聘
		  } else if(interviewtype==2){
				String pk_active = (String) activevo.getPk_active();
				StringBuffer strBuffer = new StringBuffer();
				strBuffer.append("select count(*)");
				strBuffer.append("from  rm_active  a  inner  join rm_publish  b");
				strBuffer.append(" on  a.pk_active = b.pk_activity");
				strBuffer.append(" left join rm_publishplace c");
				strBuffer.append(" on  b.pk_publishjob =  c.pk_publishjob");
				strBuffer.append(" where  c.place = 2 and a.pk_active = '"+pk_active+"'");
				try {
					BaseDAO dao = new BaseDAO();
					Object[] count = (Object[]) dao.executeQuery(
							strBuffer.toString(), new ArrayProcessor());
					 if(Integer.parseInt(count[0].toString()) > 0 ){
							superVOlist.add((SuperVO) activevo);
					 }
				} catch (BusinessException e) {
					e.printStackTrace();
				}
			  }
		
		}
		
	}

    SuperVO[] vos = DatasetUtil.paginationMethod(ds, (SuperVO[])superVOlist.toArray(new SuperVO[0]));
    new SuperVO2DatasetSerializer().serialize(vos, ds, 0);
    ds.setRowSelectIndex(Integer.valueOf(0));
    SessionBean bean = SessionUtil.getRMWebUnLoginSessionBean();
    bean.setBrower_jobtyle(2);
  }
  

  private boolean isPagination(Dataset ds)
  {
    PaginationInfo pg = ds.getCurrentRowSet().getPaginationInfo();
    return pg.getRecordsCount() > 0;
  }

  public void showDetail(ScriptEvent scriptEvent)
  {
    String primarykey = getLifeCycleContext().getParameter("dsMain_primaryKey");
    SessionBean bean = SessionUtil.getRMWebUnLoginSessionBean();
    bean.setExtendAttribute("pk_active", primarykey);
    SessionBean loginBean = SessionUtil.getRMWebSessionBean();
    SessionBean unLoginBean = SessionUtil.getRMWebUnLoginSessionBean();
	
 		//获取前台jsp界面的type来区分校招和社招，type=1 社招，校招
 		int interviewtype = unLoginBean.getType();
 		
 		if(interviewtype ==0){
 			
 			CommonUtil.showMessageDialog("获取界面数据数据异常");	
 		}		
 		if (interviewtype==1){
 			
 			if (loginBean == null) {
 				sendRedirectLogin("/app/RMWebSchlRmPostApp");
 			} else {
 				sendRedirectLogin("/app/RMWebLoginSociIndexApp");
 			}
 		}else{
 			if (loginBean == null) {
 				sendRedirectLogin("/app/RMWebSchlRmPostApp");
 			} else {
 				sendRedirectLogin("/app/RMWebLoginSociIndexApp");
 			}
 			
 		}  
   
   
  }
  
  protected AppLifeCycleContext getLifeCycleContext() {
    return AppLifeCycleContext.current();
  }


  public void toAllPostList(LinkEvent linkEvent)
  {
    SessionBean bean = SessionUtil.getRMWebUnLoginSessionBean();
    bean.setExtendAttribute("pk_active", "");
    SessionBean loginBean = SessionUtil.getRMWebSessionBean();
    SessionBean unLoginBean = SessionUtil.getRMWebUnLoginSessionBean();
	
	//获取前台jsp界面的type来区分校招和社招，type=1 社招，校招
	int  interviewtype = unLoginBean.getType();
	
	if(interviewtype ==0){
		
		CommonUtil.showMessageDialog("获取界面数据数据异常");	
	}
	
	if (interviewtype==1){
		
		if (loginBean == null) {
			sendRedirectLogin("/app/RMWebSchlRmPostApp?type=1");
		} else {
			sendRedirectLogin("/app/RMWebLoginSociIndexApp");
		}
	}else{
		if (loginBean == null) {
			sendRedirectLogin("/app/RMWebSchlRmPostApp?type=2");
		} else {
			sendRedirectLogin("/app/RMWebLoginSociIndexApp");
		}
		
	}
	

  }
  


  private void sendRedirectLogin(String app)
  {
    ApplicationContext appCtx = AppLifeCycleContext.current().getApplicationContext();
    String url = LfwRuntimeEnvironment.getRootPath() + app;
    appCtx.sendRedirect(url);
  }

  public void toRmFlow(LinkEvent linkEvent)
  {
    sendRedirectLogin("/rmweb_welcome_flow.html");
  }
  


  public void lnkRegist(LinkEvent linkEvent)
  {
    String appId = "/app/RMWebRegistApp?returnAppId=" + AppUtil.getCntAppCtx().getAppId();
    sendRedirectLogin(appId);
  }
  

  public void lnkRmWebLogin(LinkEvent linkEvent)
  {
	  String  id = AppUtil.getCntAppCtx().getAppId();
    String appId = "/app/RMWebLoginApp?returnAppId=" + AppUtil.getCntAppCtx().getAppId();
    sendRedirectLogin(appId);
  }
}
