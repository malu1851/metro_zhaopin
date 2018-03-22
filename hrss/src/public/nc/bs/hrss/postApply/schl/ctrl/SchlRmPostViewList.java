package nc.bs.hrss.postApply.schl.ctrl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.hrss.pub.DialogSize;
import nc.bs.hrss.pub.HrssConsts;
import nc.bs.hrss.pub.ServiceLocator;
import nc.bs.hrss.pub.cmd.CloseWindowCmd;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.hrss.pub.tool.CommonUtil;
import nc.bs.hrss.pub.tool.DatasetUtil;
import nc.bs.hrss.pub.tool.SessionUtil;
import nc.bs.hrss.rm.innerjobapply.ctrl.InnerJobApplyViewList;
import nc.hr.utils.MultiLangHelper;
import nc.hr.utils.ResHelper;
import nc.itf.hrss.rmweb.rmlogin.IRMLoginService;
import nc.itf.rm.IPublishQueryService;
import nc.itf.rm.IRMPsndocManageService;
import nc.itf.rm.IRMPsndocQueryMaintain;
import nc.itf.rm.IRMPsndocQueryService;
import nc.itf.uap.IUAPQueryBS;
import nc.md.model.MetaDataException;
import nc.md.persist.framework.IMDPersistenceQueryService;
import nc.md.persist.framework.MDPersistenceService;
import nc.pubitf.para.SysInitQuery;
import nc.uap.lfw.core.AppInteractionUtil;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.cmd.CmdInvoker;
import nc.uap.lfw.core.comp.LabelComp;
import nc.uap.lfw.core.comp.WebPartComp;
import nc.uap.lfw.core.ctrl.IController;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.DatasetRelation;
import nc.uap.lfw.core.data.DatasetRelations;
import nc.uap.lfw.core.data.Field;
import nc.uap.lfw.core.data.PaginationInfo;
import nc.uap.lfw.core.data.Row;
import nc.uap.lfw.core.event.DataLoadEvent;
import nc.uap.lfw.core.event.LinkEvent;
import nc.uap.lfw.core.event.MouseEvent;
import nc.uap.lfw.core.event.ScriptEvent;
import nc.uap.lfw.core.lifecycle.LifeCyclePhase;
import nc.uap.lfw.core.lifecycle.RequestLifeCycleContext;
import nc.uap.lfw.core.model.plug.TranslatedRow;
import nc.uap.lfw.core.model.util.DefaultWindowBuilder;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.LfwWindow;
import nc.uap.lfw.core.serializer.impl.SuperVO2DatasetSerializer;
import nc.uap.oba.word.merger.model.control.Text;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.querytemplate.querytree.FromWhereSQL;
import nc.ui.querytemplate.querytree.FromWhereSQLImpl;
import nc.view.tb.adjbill.dialog.MessageShowDlg;
import nc.vo.hrss.pub.SessionBean;
import nc.vo.hrss.pub.rmweb.RmUserVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.rm.active.ActiveVO;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.psndoc.RMPsndocVO;
import nc.vo.rm.psndoc.common.RMApplyStatusEnum;
import nc.vo.rm.psndoc.common.RMApplyTypeEnum;
import nc.vo.rm.psndoc.common.ResumeSourceEnum;
import nc.vo.rm.pub.IHRRMCommonConst;
import nc.vo.rm.publish.AggPublishVO;
import nc.vo.rm.publish.PublishJobVO;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import uap.web.bd.pub.AppUtil;

/**
 * @author liuhongd
 */
public class SchlRmPostViewList implements IController {
	private static final long serialVersionUID = 1L;
	public static final String DS_SCHLRMPOST = "dsSchlRmPost";
	public static final String DSMAIN_PRIMARYKEY = "dsMain_primaryKey";
	public static final String DSMAIN_PK_GROUP = "dsMain_pk_group";
	public static final String DSMAIN_PK_ORG = "dsMain_pk_org";
	public static final String DSMAIN_PK_RM_ORG = "dsMain_pk_rmorg";
	public static final String DSMAIN_PK_RM_DEPT = "dsMain_pk_rmdept";
	public static final String DSMAIN_PK_CHANNEL = "dsMain_pk_channel";
	public static final String DSMAIN_PK_ACTIVE = "dsMain_pk_active";

	/**
	 * 数据集加载事件
	 * 
	 * @param dataLoadEvent
	 */
	public void onDataLoad_dsSchlRmPost(DataLoadEvent dataLoadEvent) {

         IUAPQueryBS iuapquery = (IUAPQueryBS) NCLocator.getInstance().lookup(IUAPQueryBS.class.getName());
        
		/* 柳衍志
		 * 实例化bean来获取社招和校招类型
		 */
		SessionBean unLoginBean = SessionUtil.getRMWebUnLoginSessionBean();
		
		//获取前台jsp界面的type来区分校招和社招，type=1 社招，校招
		int  interviewtype = unLoginBean.getType();
		
		
		if (dataLoadEvent == null) {
			Dataset ds = AppLifeCycleContext.current().getViewContext().getView().getViewModels()
					.getDataset(DS_SCHLRMPOST);
			SessionBean bean = SessionUtil.getRMWebUnLoginSessionBean();
			String pk_active = null;
			if (bean != null) {
				pk_active = (String) bean.getExtendAttributeValue("pk_active");
			}
			
			//获取前台参数
			LfwView wdtMain = LfwRuntimeEnvironment.getWebContext().getPageMeta().getView("list");
			Dataset dsClass = wdtMain.getViewModels().getDataset("condition");
			HashMap<String, Object> value = DatasetUtil.getValueMap(dsClass);

			String pk_jobType = (String) value.get("jobtype"); // 职位类别
			String publishDate = (String) value.get("publishdate"); // 发布日期
			String workplace = (String) value.get("workplace"); // 工作地点
			String jobkeyword = (String) value.get("jobkeyword"); // 关键字
			UFLiteralDate publishUFDate = null;
			String publishStrDate = getQueryDate(publishDate);
			if (!StringUtil.isEmptyWithTrim(publishStrDate)) {
				publishUFDate = new UFLiteralDate(publishStrDate);
			}

			TranslatedRow translatedRow = new TranslatedRow();
			translatedRow.setValue("pk_job#pk_jobtype@0", pk_jobType);
			translatedRow.setValue("publishdate@2", publishStrDate);
			translatedRow.setValue("workplace@6", workplace);
			translatedRow.setValue("pk_job#name@6", jobkeyword);
			FromWhereSQL fromWhereSQL = null;
			AggPublishVO[] aggVOs = null;
			if (StringUtil.isEmptyWithTrim(pk_active)) {
				try {
					fromWhereSQL = CommonUtil.genFromWhereSQL(PublishJobVO.class, translatedRow);
					aggVOs = ServiceLocator.lookup(IPublishQueryService.class).queryByPlace(fromWhereSQL, interviewtype);

				} catch (BusinessException e) {
					new HrssException(e).deal();
				} catch (HrssException e) {
					new HrssException(e).alert();
				}
			} else {// 从活动跳转到职位搜索页面

				IPublishQueryService Service = null;
				try {
					Service = ServiceLocator.lookup(IPublishQueryService.class);
					aggVOs = Service.queryPublishJobByActive(pk_active, pk_jobType, workplace, publishUFDate,
							jobkeyword,interviewtype);
				} catch (BusinessException e) {
					new HrssException(e).deal();
				} catch (HrssException e) {
					e.alert();
				}
			}
			List<SuperVO> superVOlist = new ArrayList<SuperVO>();
			if (aggVOs != null) {
			
				for (AggPublishVO aggVO : aggVOs) {
					superVOlist.add((SuperVO) aggVO.getParentVO());
				}
			}
			new SuperVO2DatasetSerializer().serialize(superVOlist.toArray(new SuperVO[0]), ds, Row.STATE_NORMAL);
			InnerJobApplyViewList.setRmnumShow(ds);
			ds.setRowSelectIndex(0);
		} else {
			Dataset ds = dataLoadEvent.getSource();
			if (!isPagination(ds)) { // 分页操作
				DatasetUtil.clearData(ds);
			}
			SessionBean bean = SessionUtil.getRMWebUnLoginSessionBean();
			String pk_active = (String) bean.getExtendAttributeValue(ActiveVO.PK_ACTIVE);
			FromWhereSQL fromWhereSQL = new FromWhereSQLImpl();
			List<SuperVO> superVOlist = new ArrayList<SuperVO>();
			AggPublishVO[] aggVOs = null;
			if (StringUtil.isEmptyWithTrim(pk_active)) {
				try {										
					if(interviewtype ==0){
						
						CommonUtil.showMessageDialog("获取界面数据数据异常");	
					}
					if (interviewtype ==1){
						aggVOs = ServiceLocator.lookup(IPublishQueryService.class).queryByPlace(fromWhereSQL,1);				
					}else{
					    aggVOs = ServiceLocator.lookup(IPublishQueryService.class).queryByPlace(fromWhereSQL,2);
					}
				} catch (BusinessException e) {
					new HrssException(e).deal();
				} catch (HrssException e) {
					new HrssException(e).alert();
				}
			} else {// 从活动跳转到职位搜索页面

				LfwView view = AppLifeCycleContext.current().getViewContext().getView();
				LabelComp lblPostText = (LabelComp) view.getViewComponents().getComponent("lblPostText");

				IMDPersistenceQueryService service = MDPersistenceService.lookupPersistenceQueryService();
				ActiveVO activeVO = null;
				try {
					activeVO = service.queryBillOfVOByPK(ActiveVO.class, pk_active, false);
				} catch (MetaDataException e) {
					new HrssException(e).deal();
				}

				String activeName = MultiLangHelper.getName(activeVO);

				lblPostText.setI18nName("");
			    
				lblPostText.setText(activeName);
//				lblPostText.setText(activeName
//						+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("node_rm-res", "w_rmweb-000434")/*
//																												 * @
//																												 * res
//																												 * "校园招聘职位"
//																												 */);
				   				
				
				      /* 添加招聘简章  马鹏鹏    */
				
				        LabelComp lblrules = (LabelComp) view
					      .getViewComponents().getComponent("lblrules");	
			            lblrules.setI18nName("");
			            lblrules.setText(activeName);
				        LabelComp lblrule = (LabelComp) view
						      .getViewComponents().getComponent("lblrule");	
				        lblrule.setI18nName("");	       				    
				        String general_rules = activeVO.getGeneral_rules(); 
				         StringBuffer buf = new StringBuffer("<div style=\"padding-right:10px; padding-left:10px; scrollbar-face-color:#ffffff; font-size:14pt;font-weight:bold;padding-bottom:0px; scrollbar-highlight-color:#ffffff; width:97%; scrollbar-shadow-color:#919192; scrollbar-3dlight-color:#ffffff; line-height:100%; scrollbar-arrow-color:#919192; padding-top:0px; scrollbar-track-color:#ffffff; font-family:宋体; scrollbar-darkshadow-color:#ffffff; letter-spacing:1pt; height:260px; text-align:left;overflow:auto;border:1px solid  #919192;margin:3px;padding:5px;word-break:break-all;\">"); 		        				        	 
				        if(general_rules!=null){
				        	//buf.append(general_rules);
				        	StringBuffer sb = new StringBuffer();
				        	//buf.append(strToSB(general_rules,sb).toString());
				        	buf.append(general_rules.replace("&", "&nbsp;").replace("br","<br>"));
				        	buf.append("</div>");	
				        	//StringBuffer sb = new StringBuffer();			        	
				        	//lblrule.setInnerHTML(strToSB(general_rules,sb,74).toString());
				        	lblrule.setInnerHTML(buf.toString());
				        }else{				      	
				        	 buf.append("无简章内容");
				        	 buf.append("</div>");	
				        	lblrule.setInnerHTML(buf.toString());
				        }					       				       					       				        			       				        
				IPublishQueryService  Service = null;
				try {
					Service = ServiceLocator.lookup(IPublishQueryService.class);					
					//aggVOs = Service.queryPublishJobByActive(pk_active, null, null, null, null);
					
					/* 柳衍志
					 * 根据发布地方查询全部岗位
					 */
					aggVOs = Service.queryByPlace(null, interviewtype);
					
				} catch (BusinessException e) {
					new HrssException(e).deal();
				} catch (HrssException e) {
					e.alert();
				}
			}
			if (!ArrayUtils.isEmpty(aggVOs)) {
				for (AggPublishVO aggVO : aggVOs) {										  
					/* 柳衍志
					 * 根据活动主键来限制显示本活动的岗位，和显示所有的岗位
					 */					
					// 获取PublishJobVO
					PublishJobVO publishjobvo = (PublishJobVO) aggVO
							.getParentVO();
					// 获取职位主键
					String pk_activity = publishjobvo.getPk_activity();					
					if(StringUtil.isEmptyWithTrim(pk_active)||pk_active.equals(pk_activity)){						
						superVOlist.add((SuperVO) aggVO.getParentVO());						
					}					
				}
			}
			SuperVO[] vos = DatasetUtil.paginationMethod(ds, superVOlist.toArray(new SuperVO[0]));
			new SuperVO2DatasetSerializer().serialize(vos, ds, Row.STATE_NORMAL);
			InnerJobApplyViewList.setRmnumShow(ds);
			ds.setRowSelectIndex(0);
		}
	}

	/**
	 * 分页操作标志
	 * 
	 * @param ds
	 * @return
	 */
	private boolean isPagination(Dataset ds) {
		PaginationInfo pg = ds.getCurrentRowSet().getPaginationInfo();
		return pg.getRecordsCount() > 0;
	}

	/**
	 * 查询方法
	 * 
	 * @param mouseEvent
	 */
	@SuppressWarnings("rawtypes")
	public void onSearchPost(MouseEvent mouseEvent) {
		onDataLoad_dsSchlRmPost(null);
	}

	/**
	 * 查看职位详情
	 * 
	 * @param scriptEvent
	 */
	public void showPostDetail(ScriptEvent scriptEvent) {
		// 职位主键
		String primarykey = getLifeCycleContext().getParameter(DSMAIN_PRIMARYKEY);
		getLifeCycleContext().getApplicationContext().addAppAttribute(DSMAIN_PRIMARYKEY, primarykey);
		// ApplicationContext appCtx =
		// AppLifeCycleContext.current().getApplicationContext();
		// appCtx.navgateTo("WebJobDetail",
		// nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res","0c_hrss-res0002")/*@res
		// "职位详细内容"*/, String.valueOf(DialogSize.LARGE.getWidth()),
		// String.valueOf(DialogSize.LARGE.getHeight()), null,
		// ApplicationContext.TYPE_DIALOG, false);
		CommonUtil.showWindowDialog("WebJobDetail",
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res", "0c_hrss-res0002")/*
																									 * @
																									 * res
																									 * "职位详细内容"
																									 */,
				String.valueOf(DialogSize.LARGE.getWidth()), String.valueOf(DialogSize.LARGE.getHeight()), null,
				ApplicationContext.TYPE_DIALOG, false, true);
	}

	/**
	 * 申请职位
	 * 
	 * @param scriptEvent
	 */
	public void approvePost(ScriptEvent scriptEvent) {
		String primarykey = getLifeCycleContext().getParameter(DSMAIN_PRIMARYKEY);
		String pk_group = getLifeCycleContext().getParameter(DSMAIN_PK_GROUP);
		String pk_org = getLifeCycleContext().getParameter(DSMAIN_PK_ORG);
		String pk_rmorg = getLifeCycleContext().getParameter(DSMAIN_PK_RM_ORG);
		String pk_rmdept = getLifeCycleContext().getParameter(DSMAIN_PK_RM_DEPT);
		String pk_channel = getLifeCycleContext().getParameter(DSMAIN_PK_CHANNEL);
		String pk_active = getLifeCycleContext().getParameter(DSMAIN_PK_ACTIVE);
		SessionBean bean = SessionUtil.getRMWebUnLoginSessionBean();
		bean.setExtendAttribute(RMPsnJobVO.PK_CHANNEL, pk_channel);
		bean.setExtendAttribute(RMPsnJobVO.PK_ACTIVE, pk_active);
		doApprovePost(primarykey, pk_group, pk_org, pk_rmorg, pk_rmdept);
	}

	/**
	 * 申请职位
	 * 
	 * @param primarykey
	 * @param pk_group
	 * @param pk_org
	 * @param pk_rmdept
	 */
	public static void doApprovePost(String primarykey, String pk_group, String pk_org, String pk_rmorg,
			String pk_rmdept) {
		// 判断是否登陆了
		RmUserVO rmUserVO = validateLogin(primarykey);
		if (rmUserVO == null) {
			return;
		}
		String pk_psndoc = rmUserVO.getHrrmpsndoc();
		// 如果还没有填写简历，则跳转到简历注册页面
		if (StringUtil.isEmptyWithTrim(pk_psndoc)) {
			// 校园和社会招聘登录后共用一个页面
			sendRedirectLogin("/app/RMWebLoginSociIndexApp?pk_job=" + primarykey);
		} else {
			//判断是否已上传照片
			AggRMPsndocVO aggVO = getAggRMPsndocVO(pk_psndoc);
			// 简历主表VO
			RMPsndocVO mainVO = (RMPsndocVO) aggVO.getParentVO();
			if(mainVO.getPhoto()==null){
				AppInteractionUtil.showMessageDialog("您还未上传照片，不能申请职位");
				return;
			}
			// 申请职位前校验
			if (checkApplyPost(pk_psndoc, primarykey)) {// 申请职位前校验
				if (checkJobOrg(pk_psndoc, pk_org)) {
					if (checkApprovePost(pk_psndoc, getParamJob(pk_group))) {
						//生源地mainVO.getPermanreside()
						// 区分校招社招
						SessionBean unLoginBean = SessionUtil
								.getRMWebUnLoginSessionBean();
						if (HrssConsts.RMWEBJOBTYPE_SCHOOL == unLoginBean
								.getType()) {
							if (mainVO.getPermanreside() == null) {
								AppInteractionUtil
										.showMessageDialog("请先完善校园简历！");// 校招
								return;
							}
						} else {

							if (mainVO.getExpect_wage() == null) {
								AppInteractionUtil
										.showMessageDialog("请先完善社会简历！");// 社招
								return;
							}
						}
						if (!showConfirmDialog("只能申请一个岗位，一经投递，不能更改")) {
							return;
						}
						// 申请职位前校验
						 //validateResume(aggVO);
						UFLiteralDate nowDate = new UFLiteralDate();
						// 增加职位申请表记录
						RMPsnJobVO rmpsnJobVO = new RMPsnJobVO();
						rmpsnJobVO.setStatus(VOStatus.NEW);
						rmpsnJobVO.setPk_group(pk_group);
						rmpsnJobVO.setPk_org(pk_org);
						rmpsnJobVO.setPk_reg_job(primarykey);
						rmpsnJobVO.setPk_psndoc(pk_psndoc);
						rmpsnJobVO.setPk_reg_org(pk_rmorg);
						rmpsnJobVO.setPk_reg_dept(pk_rmdept);
						rmpsnJobVO.setReg_date(nowDate);
						rmpsnJobVO.setApplystatusdate(nowDate);
						rmpsnJobVO.setIsuse(UFBoolean.TRUE);
						rmpsnJobVO.setIspsnlib(UFBoolean.FALSE);
					
						if (HrssConsts.RMWEBJOBTYPE_SCHOOL == unLoginBean.getType()) {
							rmpsnJobVO.setSourcetype(ResumeSourceEnum.PLATFORM_SCH.toIntValue());// 校招
						} else {
							rmpsnJobVO.setSourcetype(ResumeSourceEnum.PLATFORM_SOC.toIntValue());// 社招
						}
						// 应聘渠道
						String pk_channel = (String) unLoginBean.getExtendAttributeValue(RMPsnJobVO.PK_CHANNEL);
						String pk_active = (String) unLoginBean.getExtendAttributeValue(RMPsnJobVO.PK_ACTIVE);
						rmpsnJobVO.setPk_active(pk_active);
						rmpsnJobVO.setPk_channel(pk_channel);

						// 应聘类型是再聘时应聘状态设为INIT
						if (RMApplyTypeEnum.REAPPLY.toIntValue() == mainVO.getApplytype()) {
							rmpsnJobVO.setApplystatus((Integer) RMApplyStatusEnum.INIT.value()); // 应聘状态
						} else {
							rmpsnJobVO.setApplystatus((Integer) RMApplyStatusEnum.APPLY.value()); // 应聘状态
						}
						// 应聘人员应聘/取消应聘职位
						saveApplyJob(rmpsnJobVO);
						CmdInvoker.invoke(new CloseWindowCmd());
					} else {
						AppInteractionUtil
								.showMessageDialog(MessageFormat.format(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
										.getStrByID("c_hrss-res", "0c_hrss-res0004")/*
																					 * @
																					 * res
																					 * "您最多只能申请{0}个职位！"
																					 */, 1));
					}
				} else {
					AppInteractionUtil.showMessageDialog(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
							"c_hrss-res", "0c_hrss-res0005")/*
															 * @res
															 * "您只能申请一个公司下的职位！"
															 */);
				}

			} else {
				AppInteractionUtil.showMessageDialog(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
						"c_hrss-res", "0c_hrss-res0006")/*
														 * @res
														 * "应聘职位存在重复记录,申请失败！"
														 */);
			}

		}
	}

	/**
	 * 判断是否登陆了
	 * 
	 * @param primarykey
	 */
	private static RmUserVO validateLogin(String primarykey) {
		// 从Session中取出用户信息
		SessionBean bean = SessionUtil.getRMWebSessionBean();
		if (bean == null) {
			sendRedirectLogin("/app/RMWebLoginApp?pk_job=" + primarykey+"&returnAppId="+ AppUtil.getCntAppCtx().getAppId());
			return null;
		}
		RmUserVO rmUserVO = bean.getRmUserVO();
		if (rmUserVO == null) {
			// 没有登陆跳转到登陆页面
			sendRedirectLogin("/app/RMWebLoginApp?pk_job=" + primarykey+"&returnAppId="+ AppUtil.getCntAppCtx().getAppId());
			return null;
		}
		return rmUserVO;
	}

	/**
	 * 应聘职位数
	 * 
	 * @param pk_group
	 * @return
	 */
	private static Integer getParamJob(String pk_group) {
		Integer paramJob = null;
		try {
			paramJob = SysInitQuery.getParaInt(pk_group, IHRRMCommonConst.PARAM_JOB);
		} catch (BusinessException e2) {
			new HrssException(e2).deal();
		}
		return paramJob;
	}

	/**
	 * 应聘者简历
	 * 
	 * @param pk_psndoc
	 * @return
	 */
	public static AggRMPsndocVO getAggRMPsndocVO(String pk_psndoc) {
		// 根据简历主键查询简历aggVO
		AggRMPsndocVO aggVO = null;
		try {
			aggVO = ServiceLocator.lookup(IRMPsndocQueryMaintain.class).queryByPK(pk_psndoc);
		} catch (BusinessException e1) {
			new HrssException(e1).deal();
		} catch (HrssException e1) {
			e1.alert();
		}
		return aggVO;
	}

	/**
	 * 应聘人员应聘/取消应聘职位
	 * 
	 * @param rmpsnJobVO
	 */
	private static void saveApplyJob(RMPsnJobVO rmpsnJobVO) {
		try {
			IRMPsndocManageService service = ServiceLocator.lookup(IRMPsndocManageService.class);
			service.saveApplyJob(rmpsnJobVO);
		} catch (DAOException e) {
			new HrssException(e).deal();
		} catch (HrssException e) {
			e.deal();
		} catch (BusinessException e) {
			new HrssException(e).alert();
		}
	}

	/**
	 * 检验内聘简历完整性
	 * 
	 * @param aggRMPsndocVO
	 */
	private static void validateResume(AggRMPsndocVO aggRMPsndocVO) {
		RequestLifeCycleContext.get().setPhase(LifeCyclePhase.nullstatus);
		// Map<String, Object> param = new HashMap<String, Object>();
		// param.put(WebConstant.PAGE_ID_KEY, "resumemanage");
		// LfwWindow pm = new DefaultPageMetaBuilder().buildPageMeta(param);
		// QXP63 有问题需测试
		LfwWindow pm = null;
		// 区分校招社招
		SessionBean unLoginBean = SessionUtil.getRMWebUnLoginSessionBean();
		if (HrssConsts.RMWEBJOBTYPE_SCHOOL == unLoginBean.getBrower_jobtyle()) {
			pm = new DefaultWindowBuilder().buildPageMeta("schlResume"); // 校招
		} else {
			pm = new DefaultWindowBuilder().buildPageMeta("SctyResume"); // 社招
		}

		LfwView widget = pm.getView("main");
		// 主数据集
		Dataset masterDs = widget.getViewModels().getDataset("dsBasicInfo");
		RMPsndocVO rmPsndocVO = aggRMPsndocVO.getPsndocVO();
		validateSuperVO(masterDs, rmPsndocVO);
		DatasetRelations dsRels = widget.getViewModels().getDsrelations();
		DatasetRelation[] masterRels = dsRels.getDsRelations(masterDs.getId());
		for (int i = 0; i < masterRels.length; i++) {
			// 获取子对应的外键值，并设置到VO条件中
			DatasetRelation dr = masterRels[i];
			Dataset detailDs = widget.getViewModels().getDataset(dr.getDetailDataset());
			CircularlyAccessibleValueObject[] childVOs = aggRMPsndocVO.getTableVO(detailDs.getId());
			if (!detailDs.isNotNullBody() && ArrayUtils.isEmpty(childVOs))
				CommonUtil.showErrorDialog(ResHelper.getString("c_pub-res", "0c_pub-res0168"),
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_rm-res", "0c_rm-res0040")/*
																										 * @
																										 * res
																										 * "简历不完整，请维护个人简历"
																										 */);
			if (ArrayUtils.isEmpty(childVOs))
				continue;
			for (CircularlyAccessibleValueObject childVO : childVOs)
				validateSuperVO(detailDs, (SuperVO) childVO);
		}
	}

	private static void validateSuperVO(Dataset ds, SuperVO superVO) {
		Field[] fields = ds.getFieldSet().getFields();
		for (int k = 0; k < fields.length; k++) {
			Field field = fields[k];
			Object value = superVO.getAttributeValue(field.getId());
			if (!field.isNullAble() && (!field.getId().endsWith("_name")) && (value == null || value.equals(""))) {
				CommonUtil.showErrorDialog(ResHelper.getString("c_pub-res", "0c_pub-res0168"),
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_rm-res", "0c_rm-res0040")/*
																										 * @
																										 * res
																										 * "简历不完整，请维护个人简历"
																										 */);
			}
		}
	}

	/**
	 * 根据参数限制申请的职位数目
	 * 
	 * @param pk_psndoc
	 * @param pk_org
	 * @return
	 */
	public static boolean checkApprovePost(String pk_psndoc, Integer paramJob) {

		try {
			IRMPsndocQueryService service = ServiceLocator.lookup(IRMPsndocQueryService.class);
			int coutApproveJob = service.getApplyJobCount(pk_psndoc);
			if (coutApproveJob >= 1) {
				return false;
			}
		} catch (HrssException e) {
			e.deal();
		} catch (BusinessException e) {
			new HrssException(e).deal();
		}
		return true;
	}

	/**
	 * 校验是否重复申请
	 * 
	 * @param pk_psndoc
	 * @return
	 */
	public static boolean checkApplyPost(String pk_psndoc, String pk_reg_job) {
		UFDouble jobs = null;
		try {
			IRMLoginService service = ServiceLocator.lookup(IRMLoginService.class);
			jobs = service.checkHrrmpsndoc(pk_psndoc, pk_reg_job);
		} catch (BusinessException e) {
			new HrssException(e).deal();
		} catch (HrssException e) {
			e.alert();
		}
		if (jobs.equals(UFDouble.ZERO_DBL)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 校验是否重复申请
	 * 
	 * @param pk_psndoc
	 * @return
	 */
	public static boolean checkJobOrg(String pk_psndoc, String pk_org) {

		// OrgVO orgVO = null;
		// try {
		// IRMLoginService service =
		// ServiceLocator.lookup(IRMLoginService.class);
		// orgVO = service.checkJobOrg(pk_psndoc);
		// } catch (BusinessException e) {
		// new HrssException(e).deal();
		// } catch (HrssException e) {
		// e.alert();
		// }
		// if (orgVO!= null) {
		// if (pk_org.equals(orgVO.getPk_org())) {
		// return true;
		// } else {
		// return false;
		// }
		// }
		return true;
	}

	/**
	 * 跳转页面
	 */
	private static void sendRedirectLogin(String app) {
		ApplicationContext appCtx = AppLifeCycleContext.current().getApplicationContext();
		String url = LfwRuntimeEnvironment.getRootPath() + app;
		appCtx.sendRedirect(url);
	}

	public static AppLifeCycleContext getLifeCycleContext() {
		return AppLifeCycleContext.current();
	}

	/**
	 * 查询条件数据集加载事件
	 * 
	 * @param dataLoadEvent
	 */
	public void onConditionDataLoad(DataLoadEvent dataLoadEvent) {
		Dataset ds = dataLoadEvent.getSource();
		Row row = ds.getEmptyRow();
		Integer index = 0;
		ds.insertRow(index, row);
		ds.setEnabled(true);
		ds.setRowSelectIndex(index);
	}

	private String getQueryDate(String publishdate) {
		if (StringUtils.isEmpty(publishdate))
			return null;
		Calendar cal = Calendar.getInstance();
		if ("1".equals(publishdate)) {
			cal.add(Calendar.DATE, -3); // 近三天
		} else if ("2".equals(publishdate)) {
			cal.add(Calendar.DATE, -7); // 近一周
		} else if ("3".equals(publishdate)) {
			cal.add(Calendar.DATE, -14); // 近两周
		} else if ("4".equals(publishdate)) {
			cal.add(Calendar.MONTH, -1); // 近一个月
		} else if ("5".equals(publishdate)) {
			cal.add(Calendar.MONTH, -2); // 近两个月
		} else {
			return null;
		}

		String str = UFLiteralDate.getDate(cal.getTime()).toString();
		return str;
	}

	@SuppressWarnings("rawtypes")
	public void onBack(MouseEvent mouseEvent) {
		sendRedirectLogin("/app/RMWebSchoolIndexApp");
	}

	@SuppressWarnings("rawtypes")
	public void plugininid_soci(Map keys) {
		String app = (String) SessionUtil.getRMWebUnLoginSessionBean().getExtendAttributeValue("app");
		sendRedirectLogin(app);
	}

	/**
	 * 跳转到注册页面
	 * 增加appId
	 * 马鹏鹏
	 * 
	 */
	public void lnkRegist(LinkEvent linkEvent) {
		//sendRedirectLogin("/app/RMWebRegistApp");
		
		 String appId = "/app/RMWebRegistApp?returnAppId=" + AppUtil.getCntAppCtx().getAppId();
		    sendRedirectLogin(appId);
			
	}

	/**
	 * 跳转到登陆页面
	 * 增加appId
	 * 马鹏鹏 
	 */
	public void lnkRmWebLogin(LinkEvent linkEvent) {
		//sendRedirectLogin("/app/RMWebLoginApp");
		 String appId = "/app/RMWebLoginApp?returnAppId=" + AppUtil.getCntAppCtx().getAppId();
		  sendRedirectLogin(appId);
	}
	
	
	/**
	 * 确认申请对话框
	 * @param msg
	 * @return
	 */
	private static boolean showConfirmDialog(String msg){
        //return CommonUtil.showConfirmDialog(ResHelper.getString("node_rmweb-res", "w_rmweb-000213")/*@res"确认对话"*/, msg);
		return CommonUtil.showConfirmDialog("确认对话", msg);
	}
	/**
	 * 截取字符传放到StringBuffer中
	 * 
	 * @param 马鹏鹏
	 */
	public StringBuffer strToSB(String str,StringBuffer sb,int sum) {
		 int index = str.length()/sum+1;
	        for(int i=0;i<index;i++){
	        	if(str.length()<sum){
	        		sb.append(str.substring(0,str.length()));
			    	break;   
			    }			        	
	        	sb.append(str.substring(0,sum)+"<br>");
	        	str =str.substring(sum);
	        }
	        return sb;
	}	
	
}