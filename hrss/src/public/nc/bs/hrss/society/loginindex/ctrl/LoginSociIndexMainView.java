package nc.bs.hrss.society.loginindex.ctrl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nc.bs.hrss.postApply.schl.ctrl.SchlRmPostViewList;
import nc.bs.hrss.pub.DialogSize;
import nc.bs.hrss.pub.HrssConsts;
import nc.bs.hrss.pub.ServiceLocator;
import nc.bs.hrss.pub.exception.HrssException;
import nc.bs.hrss.pub.tool.CommonUtil;
import nc.bs.hrss.pub.tool.DatasetUtil;
import nc.bs.hrss.pub.tool.SessionUtil;
import nc.bs.hrss.rm.innerjobapply.InnerJobApplyConsts;
import nc.bs.hrss.rm.innerjobapply.ctrl.InnerJobApplyViewList;
import nc.bs.hrss.rmLogin.rmLogin.ctrl.RMLoginViewMain;
import nc.hr.utils.MultiLangHelper;
import nc.hr.utils.ResHelper;
import nc.itf.rm.IPublishQueryService;
import nc.md.model.MetaDataException;
import nc.md.persist.framework.IMDPersistenceQueryService;
import nc.md.persist.framework.MDPersistenceService;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.cmd.UifPlugoutCmd;
import nc.uap.lfw.core.comp.LabelComp;
import nc.uap.lfw.core.ctrl.IController;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.PaginationInfo;
import nc.uap.lfw.core.data.Row;
import nc.uap.lfw.core.event.DataLoadEvent;
import nc.uap.lfw.core.event.LinkEvent;
import nc.uap.lfw.core.event.MouseEvent;
import nc.uap.lfw.core.event.ScriptEvent;
import nc.uap.lfw.core.model.plug.TranslatedRow;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.serializer.impl.SuperVO2DatasetSerializer;
import nc.ui.querytemplate.querytree.FromWhereSQL;
import nc.ui.querytemplate.querytree.FromWhereSQLImpl;
import nc.vo.hrss.pub.SessionBean;
import nc.vo.hrss.pub.rmweb.RmUserVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.rm.active.ActiveVO;
import nc.vo.rm.psndoc.RMPsnJobVO;
import nc.vo.rm.publish.AggPublishVO;
import nc.vo.rm.publish.PublishJobVO;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author lihha
 */
public class LoginSociIndexMainView implements IController {
	private static final long serialVersionUID = 1L;
	// ְλ����
	public static final String DSMAIN_PRIMARYKEY = "dsMain_primaryKey";

	public void onDataLoad(DataLoadEvent dataLoadEvent) {
		SessionBean bean = SessionUtil.getRMWebUnLoginSessionBean();
		String pk_active = (String) bean
				.getExtendAttributeValue(ActiveVO.PK_ACTIVE);
		int browerJobtype = bean.getType();
		if (dataLoadEvent == null) {

			Dataset conditionDs = getView().getViewModels().getDataset(
					"condition");
			Row row = conditionDs.getSelectedRow();
			String jobtype = (String) row.getValue(conditionDs
					.nameToIndex("jobtype")); // ְҵ���
			String publishdate = (String) row.getValue(conditionDs
					.nameToIndex("publishdate")); // ��������
			UFLiteralDate publishUFDate = null;
			String publishStrDate = getQueryDate(publishdate);
			if (!StringUtil.isEmptyWithTrim(publishStrDate)) {
				publishUFDate = new UFLiteralDate(publishStrDate);
			}
			String workplace = (String) row.getValue(conditionDs
					.nameToIndex("workplace")); // �����ص�
			String jobkeyword = (String) row.getValue(conditionDs
					.nameToIndex("jobkeyword")); // �ؼ���
			TranslatedRow translatedRow = new TranslatedRow();
			translatedRow.setValue("pk_job#pk_jobtype@0", jobtype);
			translatedRow.setValue("publishdate@2", getQueryDate(publishdate));
			translatedRow.setValue("workplace@6", workplace);
			translatedRow.setValue("pk_job#name@6", jobkeyword);
			FromWhereSQL fromWhereSQL = null;
			AggPublishVO[] aggVOs = null;
			if (StringUtil.isEmptyWithTrim(pk_active)) {
				try {
					fromWhereSQL = CommonUtil.genFromWhereSQL(
							PublishJobVO.class, translatedRow);
					aggVOs = ServiceLocator.lookup(IPublishQueryService.class)
							.queryByPlace(fromWhereSQL, browerJobtype);

				} catch (BusinessException e) {
					new HrssException(e).deal();
				} catch (HrssException e) {
					new HrssException(e).alert();
				}
			} else {// �ӻ��ת��ְλ����ҳ��

				IPublishQueryService Service = null;
				try {
					Service = ServiceLocator.lookup(IPublishQueryService.class);
					
					  aggVOs = Service.queryPublishJobByActive(pk_active,
					  jobtype, workplace, publishUFDate, jobkeyword,browerJobtype);
					 
					/**
					 * liuyanhi ���ݷ����ط�����ѯְλ
					 */
					//aggVOs = Service.queryByPlace(fromWhereSQL, browerJobtype);
				} catch (BusinessException e) {
					new HrssException(e).deal();
				} catch (HrssException e) {
					e.alert();
				}
			}
			List<SuperVO> superVOlist = new ArrayList<SuperVO>();
			if (aggVOs != null) {
				for (AggPublishVO aggVO : aggVOs) {
					/**
					 * liuyanhi ��ȡPublishJobVO
					 */
					PublishJobVO publishjobvo = (PublishJobVO) aggVO
							.getParentVO();
					// ��ȡְλ����
					String pk_activity = publishjobvo.getPk_activity();
					if (pk_active.equals(pk_activity) || pk_active.equals("")) {
						superVOlist.add((SuperVO) aggVO.getParentVO());
					}
				}
			}
			Dataset mainDs = getView().getViewModels().getDataset("rm_publish");
			new SuperVO2DatasetSerializer().serialize(
					superVOlist.toArray(new SuperVO[0]), mainDs,
					Row.STATE_NORMAL);
			InnerJobApplyViewList.setRmnumShow(mainDs);
			mainDs.setRowSelectIndex(0);
			getView().getViewComponents().getComponent("morejob_link")
					.setVisible(false);
		} else {
			LabelComp newjob_label = (LabelComp) getView().getViewComponents()
					.getComponent("newjob_label");
			newjob_label.setI18nName("");

			/* �����Ƹ���� ������ */
		
			  LabelComp lblrules = (LabelComp) getView().getViewComponents()
			  .getComponent("lblrules"); lblrules.setI18nName("");
			  
			  LabelComp lblrule = (LabelComp) getView().getViewComponents()
			  .getComponent("lblrule"); lblrule.setI18nName("");
			 
			getView().getViewComponents().getComponent("morejob_link")
					.setVisible(false);
			if (!StringUtil.isEmptyWithTrim(pk_active)) {
				IMDPersistenceQueryService service = MDPersistenceService
						.lookupPersistenceQueryService();
				ActiveVO activeVO = null;
				try {
					activeVO = service.queryBillOfVOByPK(ActiveVO.class,
							pk_active, false);
				} catch (MetaDataException e) {
					new HrssException(e).deal();
				}

				String activeName = MultiLangHelper.getName(activeVO);
				newjob_label.setText(activeName);
				 newjob_label
				 .setText(activeName
				 + nc.vo.ml.NCLangRes4VoTransl
				 .getNCLangRes().getStrByID(
				 "node_rm-res",
				 "w_rmweb-000434")/*
				 * @res
				 * "У԰��Ƹְλ"
				 */);
				/**
				 * �����Ƹ���� ������
				 */
				
				  lblrules.setText(activeName); 
				  String general_rules =activeVO.getGeneral_rules();
				  StringBuffer buf = new StringBuffer("<div style=\"padding-right:10px; padding-left:10px; scrollbar-face-color:#ffffff; font-size:14pt;font-weight:bold; padding-bottom:0px; scrollbar-highlight-color:#ffffff; width:97%; scrollbar-shadow-color:#919192; scrollbar-3dlight-color:#ffffff; line-height:100%; scrollbar-arrow-color:#919192; padding-top:0px; scrollbar-track-color:#ffffff; font-family:����; scrollbar-darkshadow-color:#ffffff; letter-spacing:1pt; height:300px; text-align:left;overflow:auto;border:1px solid #919192;margin:3px;padding:5px;word-break:break-all;\">"); 	
				  if(general_rules!=null){					 	        	
			         // buf.append(general_rules);
			          StringBuffer sb = new StringBuffer();
			         // buf.append(strToSB(general_rules,sb).toString());
			          buf.append(general_rules.replace("&", "&nbsp;").replace("br","<br>"));
			          buf.append("</div>");
				  //StringBuffer sb = new StringBuffer();
				  //lblrule.setInnerHTML(strToSB(general_rules,sb,65).toString()); 		       
			        lblrule.setInnerHTML(buf.toString()); 			    
			       }else{
		        	  buf.append("�޼�������");
		        	  buf.append("</div>");	
		        	  lblrule.setInnerHTML(buf.toString());
		        }	
			} else {
				newjob_label.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
						.getStrByID("node_rm-res", "w_rmweb-000501")/*
																	 * @res
																	 * "У԰��Ƹ����ְλ"
																	 */);
			}
			Dataset ds = getView().getViewModels().getDataset(
					InnerJobApplyConsts.DS_MAIN_NAME);
			FromWhereSQL fromWhereSQL = new FromWhereSQLImpl();
			List<SuperVO> superVOlist = new ArrayList<SuperVO>();
			AggPublishVO[] aggVOs = null;
			if (StringUtil.isEmptyWithTrim(pk_active)) {
				try {
					aggVOs = ServiceLocator.lookup(IPublishQueryService.class)
							.queryByPlace(fromWhereSQL, browerJobtype);

				} catch (BusinessException e) {
					new HrssException(e).deal();
				} catch (HrssException e) {
					new HrssException(e).alert();
				}
			} else {// �ӻ��ת��ְλ����ҳ��

				IPublishQueryService Service = null;
				try {
					Service = ServiceLocator.lookup(IPublishQueryService.class);
					/*
					 * aggVOs = Service.queryPublishJobByActive(pk_active, null,
					 * null, null, null);
					 */
					/**
					 * liuyanhi ���ݷ����ط�����ѯְλ
					 */
					aggVOs = Service.queryByPlace(fromWhereSQL, browerJobtype);
				} catch (BusinessException e) {
					new HrssException(e).deal();
				} catch (HrssException e) {
					e.alert();
				}
			}
			if (!ArrayUtils.isEmpty(aggVOs)) {
				for (AggPublishVO aggVO : aggVOs) {
					/**
					 * liuyanhi ��ȡPublishJobVO
					 */
					PublishJobVO publishjobvo = (PublishJobVO) aggVO
							.getParentVO();
					// ��ȡְλ����
					String pk_activity = publishjobvo.getPk_activity();
					if (StringUtil.isEmptyWithTrim(pk_active)||pk_active.equals(pk_activity)) {
						superVOlist.add((SuperVO) aggVO.getParentVO());
					}
				}
			}
			if (!isPagination(ds)) { // ��ҳ����
				DatasetUtil.clearData(ds);
			}
			SuperVO[] vos = DatasetUtil.paginationMethod(ds,
					superVOlist.toArray(new SuperVO[0]));
			new SuperVO2DatasetSerializer()
					.serialize(vos, ds, Row.STATE_NORMAL);
			InnerJobApplyViewList.setRmnumShow(ds);
			ds.setRowSelectIndex(0);
		}

	}

	/**
	 * ��ҳ������־
	 * 
	 * @param ds
	 * @return
	 */
	private boolean isPagination(Dataset ds) {
		PaginationInfo pg = ds.getCurrentRowSet().getPaginationInfo();
		return pg.getRecordsCount() > 0;
	}

	private LfwView getView() {
		return AppLifeCycleContext.current().getViewContext().getView();
	}

	/**
	 * ����
	 * 
	 * @param mouseEvent
	 */
	@SuppressWarnings("rawtypes")
	public void onclick(MouseEvent mouseEvent) {
		onDataLoad(null);
	}

	private String getQueryDate(String publishdate) {
		if (StringUtils.isEmpty(publishdate))
			return null;
		Calendar cal = Calendar.getInstance();
		if ("1".equals(publishdate)) {
			cal.add(Calendar.DATE, -3); // ������
		} else if ("2".equals(publishdate)) {
			cal.add(Calendar.DATE, -7); // ��һ��
		} else if ("3".equals(publishdate)) {
			cal.add(Calendar.DATE, -14); // ������
		} else if ("4".equals(publishdate)) {
			cal.add(Calendar.MONTH, -1); // ��һ����
		} else if ("5".equals(publishdate)) {
			cal.add(Calendar.MONTH, -2); // ��������
		}
		String str = UFLiteralDate.getDate(cal.getTime()).toString();
		return str;
	}

	/**
	 * ����ְλ
	 * 
	 * @param scriptEvent
	 */
	public void approvePost(ScriptEvent scriptEvent) {
		String primarykey = getLifeCycleContext().getParameter(
				SchlRmPostViewList.DSMAIN_PRIMARYKEY);
		String pk_group = getLifeCycleContext().getParameter(
				SchlRmPostViewList.DSMAIN_PK_GROUP);
		String pk_org = getLifeCycleContext().getParameter(
				SchlRmPostViewList.DSMAIN_PK_ORG);
		String pk_rmorg = getLifeCycleContext().getParameter(
				SchlRmPostViewList.DSMAIN_PK_RM_ORG);
		String pk_rmdept = getLifeCycleContext().getParameter(
				SchlRmPostViewList.DSMAIN_PK_RM_DEPT);
		SchlRmPostViewList.doApprovePost(primarykey, pk_group, pk_org,
				pk_rmorg, pk_rmdept);
	}

	/**
	 * ����ְλ
	 * 
	 * @param scriptEvent
	 */
	public void showDetail(ScriptEvent scriptEvent) {
		String pk_job = AppLifeCycleContext.current().getParameter(
				"dsMain_primaryKey");
		String pk_channel = getLifeCycleContext().getParameter(
				SchlRmPostViewList.DSMAIN_PK_CHANNEL);
		String pk_active = getLifeCycleContext().getParameter(
				SchlRmPostViewList.DSMAIN_PK_ACTIVE);
		SessionBean unLoginBean = SessionUtil.getRMWebUnLoginSessionBean();
		unLoginBean.setExtendAttribute(RMPsnJobVO.PK_CHANNEL, pk_channel);
		unLoginBean.setExtendAttribute(RMPsnJobVO.PK_ACTIVE, pk_active);
		// ��Session��ȡ���û���Ϣ
		SessionBean bean = SessionUtil.getRMWebSessionBean();
		if (bean == null) {
			// û�е�½��ת����½ҳ��
			sendRedirectLogin("/app/RMWebLoginApp");
			return;
		}
		RmUserVO rmUserVO = bean.getRmUserVO();

		String pk_psndoc = rmUserVO.getHrrmpsndoc();

		if (!SchlRmPostViewList.checkApplyPost(pk_psndoc, pk_job))
			CommonUtil.showErrorDialog(
					ResHelper.getString("c_hrss-res", "0c_hrss-res0049"),
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
							"c_hrss-res", "0c_hrss-res0006")/*
															 * @res
															 * "��ְλ������ɹ�,�벻Ҫ�ظ����룡"
															 */);

		// �����û����д����������ת������ע��ҳ��
		if (StringUtil.isEmptyWithTrim(pk_psndoc)) {
			// ApplicationContext appCtx =
			// AppLifeCycleContext.current().getApplicationContext();
			if (unLoginBean.getBrower_jobtyle() == 1) {
				// appCtx.navgateTo("SctyResume",
				// nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res","0c_hrss-res0025")/*@res
				// "�����Ƽ���"*/,"1000",
				// "720", null, ApplicationContext.TYPE_DIALOG, false);
				CommonUtil
						.showWindowDialog(
								"SctyResume",
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
										.getStrByID("c_hrss-res",
												"0c_hrss-res0025")/* @res "�����Ƽ���" */,
								LoginSociIndexLeftView.WIDTH,
								LoginSociIndexLeftView.HEIGHT, null,
								ApplicationContext.TYPE_DIALOG, false, true);
			} else {
				// appCtx.navgateTo("schlResume",
				// nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res","0c_hrss-res0025")/*@res
				// "�����Ƽ���"*/, "1000",
				// "700", null, ApplicationContext.TYPE_DIALOG, false);
				CommonUtil
						.showWindowDialog(
								"schlResume",
								nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
										.getStrByID("c_hrss-res",
												"0c_hrss-res0025")/* @res "�����Ƽ���" */,
								LoginSociIndexLeftView.WIDTH,
								LoginSociIndexLeftView.HEIGHT, null,
								ApplicationContext.TYPE_DIALOG, false, true);
			}
		} else {
			// ����ְλ������¼
			RMLoginViewMain.insertRMPsnJobVO(pk_psndoc, pk_job);
			// AppInteractionUtil.showMessage("����ɹ�");
			UifPlugoutCmd cmd = new UifPlugoutCmd("main", "jobs_changed");
			cmd.execute();
		}
	}

	/**
	 * ��תҳ��
	 */
	private void sendRedirectLogin(String app) {
		ApplicationContext appCtx = getLifeCycleContext()
				.getApplicationContext();
		String url = LfwRuntimeEnvironment.getRootPath() + app;
		appCtx.sendRedirect(url);
	}

	/**
	 * �鿴ְλ����
	 * 
	 * @param scriptEvent
	 */
	public void showPostDetail(ScriptEvent scriptEvent) {
		// ְλ����
		String primarykey = getLifeCycleContext().getParameter(
				DSMAIN_PRIMARYKEY);
		getLifeCycleContext().getApplicationContext().addAppAttribute(
				DSMAIN_PRIMARYKEY, primarykey);
		// ApplicationContext appCtx =
		// AppLifeCycleContext.current().getApplicationContext();
		// appCtx.navgateTo("WebJobDetail",
		// nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("c_hrss-res","0c_hrss-res0002")/*@res
		// "ְλ��ϸ����"*/, String.valueOf(DialogSize.MIDIUM.getWidth()),
		// String.valueOf(DialogSize.MIDIUM.getHeight()), null,
		// ApplicationContext.TYPE_DIALOG, false);
		CommonUtil.showWindowDialog(
				"WebJobDetail",
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(
						"c_hrss-res", "0c_hrss-res0002")/* @res "ְλ��ϸ����" */,
				String.valueOf(DialogSize.MIDIUM.getWidth()),
				String.valueOf(DialogSize.MIDIUM.getHeight()), null,
				ApplicationContext.TYPE_DIALOG, false, true);
	}

	public AppLifeCycleContext getLifeCycleContext() {
		return AppLifeCycleContext.current();
	}

	public void onConditionDataLoad(DataLoadEvent dataLoadEvent) {
		Dataset ds = dataLoadEvent.getSource();
		Row row = ds.getEmptyRow();
		Integer index = 0;
		ds.insertRow(index, row);
		ds.setEnabled(true);
		ds.setRowSelectIndex(index);
	}

	public void onMoreClick(LinkEvent linkEvent) {
		Dataset ds = getView().getViewModels().getDataset(
				InnerJobApplyConsts.DS_MAIN_NAME);
		FromWhereSQL fromWhereSQL = new FromWhereSQLImpl();
		/**
		 * liuyanzhi ��ȡ���к�У�еı�־λ
		 */
		SessionBean bean = SessionUtil.getRMWebUnLoginSessionBean();
		String pk_active = (String) bean
				.getExtendAttributeValue(ActiveVO.PK_ACTIVE);
		int browerJobtype = bean.getType();
		try {
			AggPublishVO[] aggVOs = ServiceLocator.lookup(
					IPublishQueryService.class).queryByPlace(fromWhereSQL,
					browerJobtype);
			List<SuperVO> superVOlist = new ArrayList<SuperVO>();
			if (aggVOs != null) {
				for (AggPublishVO aggVO : aggVOs) {
					superVOlist.add((SuperVO) aggVO.getParentVO());
				}
			}
			new SuperVO2DatasetSerializer().serialize(
					superVOlist.toArray(new SuperVO[0]), ds, Row.STATE_NORMAL);
			InnerJobApplyViewList.setRmnumShow(ds);
		} catch (BusinessException e) {
			new HrssException(e).deal();
		} catch (HrssException e) {
			new HrssException(e).alert();
		}
		getView().getViewComponents().getComponent("morejob_link")
				.setVisible(false);
	}

	/**
	 * ��ȡ�ַ����ŵ�StringBuffer��
	 * 
	 * @param ������
	 */
	public StringBuffer strToSB(String str, StringBuffer sb, int sum) {
		int index = str.length() / sum + 1;
		for (int i = 0; i < index; i++) {
			if (str.length() < sum) {
				sb.append(str.substring(0, str.length()));
				break;
			}
			sb.append(str.substring(0, sum) + "<br>");
			str = str.substring(sum);
		}
		return sb;
	}

}