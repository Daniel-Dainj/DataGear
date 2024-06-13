/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.web.controller;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.FileSource;
import org.datagear.management.domain.User;
import org.datagear.management.service.FileSourceService;
import org.datagear.persistence.PagingData;
import org.datagear.util.FileInfo;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.vo.DataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * {@linkplain FileSource}控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/fileSource")
public class FileSourceController extends AbstractController
{
	@Autowired
	private FileSourceService fileSourceService;

	public FileSourceController()
	{
		super();
	}

	public FileSourceService getFileSourceService()
	{
		return fileSourceService;
	}

	public void setFileSourceService(FileSourceService fileSourceService)
	{
		this.fileSourceService = fileSourceService;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		FileSource fileSource = new FileSource();

		setFormModel(model, fileSource, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		return "/fileSource/fileSource_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody FileSource fileSource)
	{
		checkSaveEntity(fileSource);

		User user = getCurrentUser();

		fileSource.setId(IDUtil.randomIdOnTime20());
		inflateCreateUserAndTime(fileSource, user);

		this.fileSourceService.add(fileSource);

		return optSuccessDataResponseEntity(request, fileSource);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		FileSource fileSource = getByIdForEdit(this.fileSourceService, user, id);

		setFormModel(model, fileSource, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);
		
		return "/fileSource/fileSource_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody FileSource fileSource)
	{
		checkSaveEntity(fileSource);

		User user = getCurrentUser();

		this.fileSourceService.update(user, fileSource);

		return optSuccessDataResponseEntity(request, fileSource);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		FileSource fileSource = getByIdForView(this.fileSourceService, user, id);

		setFormModel(model, fileSource, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		boolean isShowDirectory = setIsShowDirectory(request, model);
		
		if(!isShowDirectory)
			fileSource.setDirectory(null);
		
		return "/fileSource/fileSource_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		User user = getCurrentUser();

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.fileSourceService.deleteById(user, id);
		}

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		setIsShowDirectory(request, model);
		setReadonlyAction(model);
		
		return "/fileSource/fileSource_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		setSelectAction(request, model);
		setIsShowDirectory(request, model);
		
		return "/fileSource/fileSource_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<FileSource> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) DataFilterPagingQuery pagingQueryParam)
			throws Exception
	{
		User user = getCurrentUser();
		final DataFilterPagingQuery pagingQuery = inflateDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<FileSource> pagingData = this.fileSourceService.pagingQuery(user, pagingQuery,
				pagingQuery.getDataFilter());

		if(!isShowDirectory(request))
		{
			List<FileSource> items = pagingData.getItems();
			for(FileSource item : items)
				item.setDirectory(null);
		}
		
		return pagingData;
	}

	@RequestMapping(value = "/listFiles", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public FileInfo[] listFiles(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestParam("id") String id,
			@RequestParam(value="subPath", required = false) String subPath) throws Exception
	{
		User user = getCurrentUser();

		FileSource fileSource =  getByIdForView(this.fileSourceService, user, id);
		File directory = FileUtil.getDirectory(fileSource.getDirectory(), false);
		
		if(!StringUtil.isEmpty(subPath))
			directory = FileUtil.getDirectory(directory, subPath, false);
		
		return FileUtil.getFileInfos(directory);
	}
	
	protected void checkSaveEntity(FileSource fileSource)
	{
		if (isBlank(fileSource.getDirectory()))
			throw new IllegalInputException();

		File directory = FileUtil.getDirectory(fileSource.getDirectory(), false);

		if (!directory.exists())
			throw new FileSourceDirectoryNotFoundException(fileSource.getDirectory());
	}
	
	protected boolean setIsShowDirectory(HttpServletRequest request, org.springframework.ui.Model model)
	{
		boolean isShowDirectory = isShowDirectory(request);
		model.addAttribute("isShowDirectory", isShowDirectory);
		
		return isShowDirectory;
	}
	
	protected boolean isShowDirectory(HttpServletRequest request)
	{
		User user = getCurrentUser();
		return user.isAdmin();
	}
}