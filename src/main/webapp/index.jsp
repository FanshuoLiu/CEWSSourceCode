<%@ page import="java.io.File" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>SHP加密</title>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/ol3/3.20.1/ol.css">
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.1.3/css/bootstrap.min.css">
  <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
  <style>
    .custom-width {
      width: calc(100% - 20px);
    }
    .form-container {
      padding: 15px;
      border: 1px solid #ddd;
      border-radius: 5px;
      margin-bottom: 15px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
    }
    .row {
      margin-bottom: 15px;
    }
    .message-icon {
      font-size: 1.5rem;
      margin-right: 10px;
    }
    /* 模态框显示时的背景样式 */
    .modal-backdrop.show {
      backdrop-filter: blur(2px);
    }
    /* 自定义滚动条样式 */
    ::-webkit-scrollbar {
      width: 17px;
    }
    ::-webkit-scrollbar-track {
      background: transparent;
    }
    ::-webkit-scrollbar-thumb {
      background-color: rgba(156, 163, 175, 0.5);
      border-radius: 10px;
      border: 6px solid transparent;
      background-clip: content-box;
    }
    /* 模态框打开时的滚动条样式 */
    .modal-open ::-webkit-scrollbar-thumb {
      background-color: rgba(156, 163, 175, 0.2);
    }
  </style>
</head>

<body class="container mt-4">
<h1 class="text-center mb-4">加密水印系统V1.0</h1>
<!-- 页面内容保持不变 -->
<div class="row">
  <div class="col-md-4 form-container">
    <h3>文件上传</h3>
    <form method="post" id="uploadForm" action="Upload" enctype="multipart/form-data" class="mb-3">
      <label for="shpFile" class="form-label">选择一个zip或bmp图像:</label>
      <input type="file" class="form-control form-control-sm custom-width" name="uploadFile" id="shpFile"
             accept=".zip,.bmp" required>
      <p class="text-muted small">注意：添加水印仅接受BMP格式的图片文件。</p>
      <button type="button" class="btn btn-primary btn-sm" id="uploadBtn">上传</button>
    </form>
  </div>

  <div class="col-md-4 form-container">
    <h3>文件下载</h3>
    <form action="download" method="get">
      <label for="filename" class="form-label">选择要下载的文件:</label>
      <select name="filename" id="filename" class="form-select form-select-sm custom-width" required>
        <%  File lists = new File(application.getRealPath("/uploads" + File.separator));
          String[] files=lists.list(); if (files !=null) { for (String file : files) { if (file.endsWith(".zip") ||
                  file.endsWith(".bmp")){ %>
        <option value="<%= file %>">
          <%= file %>
        </option>
        <% } } } %>
      </select>
      <div class="mt-2">
        <button type="submit" class="btn btn-success btn-sm" id="downloadButton">下载</button>
      </div>
    </form>
    <button class="btn btn-info btn-sm mt-2" id="loadZipFiles">查看ZIP文件列表</button>
  </div>

  <div class="col-md-4 form-container">
    <h3>加密</h3>
    <form method="post" action="init" id="encryptedForm" class="mb-3">
      <div class="mb-2">
        <label for="filename1" class="form-label">选择shp:</label>
        <select name="filename1" id="filename1" class="form-select form-select-sm custom-width" required>
          <%String[] files1=lists.list();
            if (files1 !=null) { for (String file : files1) { if (file.endsWith(".shp")){ %>
          <option value="<%= file %>"><%= file %></option>
          <% } } } %>
        </select>
      </div>
      <div class="mb-2">
        <label for="num" class="form-label">选择要加密的份数(1到10之间)</label>
        <input type="text" name="num" id="num" class="form-control form-control-sm custom-width" required>
      </div>
      <button type="button" class="btn btn-primary btn-sm" id="encryptBtn">加密</button>
    </form>
  </div>
</div>

<div class="row">
  <div class="col-md-4 form-container">
    <h3>解密</h3>
    <form action="decrypt" method="post" id="decryptForm" class="mb-3">
      <div class="mb-2">
        <label for="selectedIndices" class="form-label">选择shp:</label>
        <select name="selectedIndices" id="selectedIndices" class="form-select form-select-sm custom-width" multiple>
          <% String[] files2=lists.list(); if (files2 !=null) { for (String file : files2) { if (file.contains("加密")
                  && file.endsWith(".shp")){ %>
          <option value="<%= file %>">
            <%= file %>
          </option>
          <% } } } %>
        </select>
      </div>

      <input type="hidden" name="allOptions" id="allOptions">
      <button type="button" class="btn btn-primary btn-sm" id="decryptBtn" name="decryptBtn">解密</button>
      <button type="button" class="btn btn-primary btn-sm" id="localDecryptBtn">局部解密</button>
    </form>
  </div>

  <div class="col-md-4 form-container">
    <h3>嵌入水印</h3>
    <form method="post" action="init" enctype="multipart/form-data" class="mb-3">
      <div class="mb-2">
        <label for="filename2" class="form-label">选择BMP文件：</label>
        <select name="filename2" id="filename2" class="form-select form-select-sm custom-width" required>
          <% String[] bmp_files=lists.list(); if (bmp_files !=null) { for (String file : bmp_files) { if
          (file.endsWith(".bmp")){ %>
          <option value="<%= file %>">
            <%= file %>
          </option>
          <% } } } %>
        </select>
      </div>
      <button type="button" class="btn btn-primary btn-sm" name="watermarkBtn" id="addWatermarkingBtn">添加水印</button>
    </form>
  </div>

  <div class="col-md-4 form-container">
    <h3>提取水印</h3>
    <form method="post" action="decrypt" enctype="multipart/form-data" class="mb-3">
      <button type="button" class="btn btn-primary btn-sm" id="extractWatermarkingBtn">提取水印</button>
    </form>
  </div>
</div>





<%-- 地图区域放在最下方 --%>
<div class="row">
  <div class="col-md-12 form-container">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3>地图显示</h3>
      <div>
        <button type="button" class="btn btn-primary btn-sm" id="loadSHPBtn">加载shp</button>
        <button type="button" class="btn btn-primary btn-sm" id="removeSHPBtn">移除地图</button>
      </div>
    </div>
    <div id="map" style="height: 500px; width: 100%;"></div>
  </div>
</div>

  <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.1.3/js/bootstrap.bundle.min.js"></script>
  <script src="https://unpkg.com/axios/dist/axios.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/ol3/3.20.1/ol.js"></script>
  <script>
    // 计算滚动条宽度
    let scrollbarWidth = 0;
    function calculateScrollbarWidth() {
      if (scrollbarWidth > 0) return scrollbarWidth;

      const scrollDiv = document.createElement('div');
      scrollDiv.style.cssText = 'width: 100px; height: 100px; overflow: scroll; position: absolute; top: -9999px;';
      document.body.appendChild(scrollDiv);
      scrollbarWidth = scrollDiv.offsetWidth - scrollDiv.clientWidth;
      document.body.removeChild(scrollDiv);

      return scrollbarWidth;
    }

    // 初始化时计算滚动条宽度
    calculateScrollbarWidth();

    // 创建通用提示模态框
    function createMessageModal(modalId, message, isLoading = false) {
      let modal = document.getElementById(modalId);
      if (!modal) {
        modal = document.createElement('div');
        modal.id = modalId;
        modal.className = 'modal fade';
        modal.tabIndex = -1;
        modal.setAttribute('aria-hidden', 'true');

        // 根据是否为加载状态设置不同的图标
        const iconHtml = isLoading
                ? '<div class="spinner-border text-primary me-3" role="status"><span class="visually-hidden">Loading...</span></div>'
                : '<div class="message-icon text-info"><i class="bi bi-info-circle"></i></div>';

        // 使用字符串拼接代替嵌套模板字符串
        let modalContent = '<div class="modal-dialog modal-sm modal-dialog-centered">';
        modalContent += '<div class="modal-content">';
        modalContent += '<div class="modal-body d-flex justify-content-center align-items-center">';
        modalContent += iconHtml;
        modalContent += '<span>' + message + '</span>';
        modalContent += '</div>';

        // 非加载状态添加关闭按钮
        if (!isLoading) {
          modalContent += '<div class="modal-footer justify-content-center">';
          modalContent += '<button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">关闭</button>';
          modalContent += '</div>';
        }

        modalContent += '</div></div>';
        modal.innerHTML = modalContent;

        document.body.appendChild(modal);

        // 添加隐藏事件处理
        modal.addEventListener('hidden.bs.modal', function() {
          // 恢复页面样式
          document.body.style.paddingRight = '';
          document.body.style.overflow = '';

          // 如果是消息提示框，关闭后移除元素
          if (!isLoading) {
            modal.remove();
          }
        });
      } else {
        // 更新已有模态框的消息
        modal.querySelector('.modal-body span').textContent = message;
      }
      return modal;
    }

    // 显示提示消息
    function showCustomAlert(message) {
      const alertModal = createMessageModal('alertModal', message, false);
      const modal = new bootstrap.Modal(alertModal);

      // 检查是否需要补偿滚动条宽度
      const hasScrollbar = document.body.scrollHeight > window.innerHeight;
      if (hasScrollbar) {
        document.body.style.paddingRight = scrollbarWidth + 'px';
        document.body.style.overflow = 'hidden';
      }

      modal.show();
    }

    // 加载指示器
    function showLoading(message = '加载中...') {
      const loadingModal = createMessageModal('loadingModal', message, true);
      const modal = new bootstrap.Modal(loadingModal);

      // 检查是否需要补偿滚动条宽度
      const hasScrollbar = document.body.scrollHeight > window.innerHeight;
      if (hasScrollbar) {
        document.body.style.paddingRight = scrollbarWidth + 'px';
        document.body.style.overflow = 'hidden';
      }
      modal.show();
    }

    function hideLoading() {
      const loadingModal = document.getElementById('loadingModal');
      if (loadingModal) {
        const modal = bootstrap.Modal.getInstance(loadingModal);
        if (modal) modal.hide();
      }
    }

    $(document).ready(function () {
      //加载zip请求
      $('#loadZipFiles').click(function () {
        $.ajax({
          url: 'zipFiles',
          type: 'POST',
          dataType: 'json',
          success: function (data) {
            const downloadFiles = data.download;
            // 刷新 decryptedSelect 下拉框
            const $selectElement3 = $('#filename');
            $selectElement3.empty(); // 清空现有选项
            $.each(downloadFiles, function (index, file) {
              const $option = $('<option></option>').val(file).text(file);
              $selectElement3.append($option); // 添加新选项
            });
          },
          error: function () {
            showCustomAlert('当前无zip文件！');
          }
        });
      });

      // //查询文件请求
      // $("#fetchFiles").click(function() {
      //     $.ajax({
      //         url: 'files',
      //         type: 'GET',
      //         dataType: 'json',
      //         success: function(data) {
      //             if (data.error) {
      //                 $("#encryptedFileCell").html(data.error);
      //                 $("#watermarkedFileCell").html('');
      //                 $("#encryptedWatermarkedFileCell").html('');
      //             } else {
      //                 let encryptedFiles = data.encrypted || [];
      //                 let watermarkedFiles = data.watermarked || [];
      //                 let encryptedAndWatermarkedFiles = data.encryptedAndWatermarked || [];
      //
      //                 // 填充表格单元格
      //                 $("#encryptedFileCell").html(encryptedFiles.join('<br>') || '无');
      //                 $("#watermarkedFileCell").html(watermarkedFiles.join('<br>') || '无');
      //                 $("#encryptedWatermarkedFileCell").html(encryptedAndWatermarkedFiles.join('<br>') || '无');
      //             }
      //         },
      //         error: function(xhr, status, error) {
      //             $("#encryptedFileCell").html('请求失败: ' + error);
      //             $("#watermarkedFileCell").html('');
      //             $("#encryptedWatermarkedFileCell").html('');
      //         }
      //     });
      // });

      // 上传AJAX请求
      $('#uploadBtn').on('click', function () {
        const formData = new FormData($('#uploadForm')[0]);
        let file=$('#shpFile').val();
        if (file == "") {
          showCustomAlert('请选择一个文件')
          return;
        }

        $.ajax({
          url: 'Upload',
          type: 'POST',
          data: formData,
          contentType: false, // 让 jQuery 自动设置 Content-Type
          processData: false, // 不处理数据
          success: function (data) {
            showCustomAlert(data.message)
            if (data.status === 'success') {
              // 上传成功后刷新文件列表
              refreshFileLists();
            }
          },
          error: function (data) {
            showCustomAlert('上传失败，网络错误或其他原因' + data.message);
          }
        });
      });

      //加密ajax请求
      $("#encryptBtn").click(function () {
        let encryptBtn = $("#encryptBtn").val();
        let filename1 = $("#filename1").val();
        let num = $("#num").val();
        if (num < 1 || num > 10 || num == "" || isNaN(num)) {
          showCustomAlert("请输入1到10之间的数字！")
          return;
        }
        // if (filename1.includes("加密")) {
        //   showCustomAlert("该文件已加密，请勿重复加密！")
        //   return;
        // } else if (filename1 == "") {
        //   showCustomAlert("请选择一个文件")
        //   return;
        // }
        $.ajax({
          type: "POST",
          url: "encrypt",
          data: {
            filename1: filename1,
            encryptBtn: encryptBtn,
            num: num
          },
          dataType: "json",
          success: function (response) {
            if (response.status === "success") {
              showCustomAlert(response.message);
              refreshFileLists();
            } else {
              showCustomAlert("错误: " + response.message);
            }
          },
          error: function (xhr, status, error) {
            showCustomAlert("请求失败: " + xhr.status + " " + error);
          }
        });
      });

      //解密ajax请求
      $('#decryptBtn').on('click', function () {
        const selectedIndices = [];
        const selectedValues = []; // 用于存储选中的内容
        const allOptions = [];
        const properties = $('#properties').val();
        // 遍历所有 <select> 的选项
        $('#selectedIndices option').each(function () {
          allOptions.push($(this).val());  // 将选项的值推入数组
        });
        $('#selectedIndices option:selected').each(function () {
          selectedIndices.push($(this).index());
          selectedValues.push($(this).val()); // 获取选中项的值
        });
        if (selectedIndices.length === 0) {
          showCustomAlert("请选择要解密的水印");
          return;
        }
        $.ajax({
          url: 'decrypt',
          type: 'POST',
          dataType: 'json',
          data: JSON.stringify({
            selectedIndices: selectedIndices,
            selectedValues: selectedValues,
            allOptions: allOptions.join(","),
            properties: properties
          }),
          success: function (response) {
            if (response.status === "success") {
              showCustomAlert(response.message);
              refreshFileLists();
            } else {
              showCustomAlert("错误:" + response.message);
            }
          },
        });
      });

      //嵌入水印ajax请求
      $("#addWatermarkingBtn").on('click', function () {
        let filename2 = $("#filename2").val();
        let filename1 = $("#filename1").val();
        if (filename2 == null || filename1 == null) {
          showCustomAlert("请选择一个shp文件和水印图像");
          return;
        }
        if (filename1.includes("水印")) {
          showCustomAlert("该文件已添加水印，请勿重复添加！");
          return;
        }
        $.ajax({
          type: "POST",
          url: "insert",
          data: { filename1: filename1, filename2: filename2 },
          dataType: "json",
          success: function (response) {
            if (response.status === "success") {
              showCustomAlert(response.message);
              refreshFileLists();
            } else {
              showCustomAlert("错误: " + response.message);
            }
          },
          error: function (xhr, status, error) {
            showCustomAlert("请求失败: " + xhr.status + " " + error);
          }
        });
      });

      //提取水印ajax请求
      $("#extractWatermarkingBtn").on('click', function () {
        let filename1 = $("#filename1").val();
        let properties = $("#properties").val();
        if (filename1 == null) {
          showCustomAlert("请选择一个含水印的shp文件");
          return;
        }
        if (properties == "") {
          showCustomAlert("请先选择一个配置文件");
          return;
        }
        $.ajax({
          type: "POST",
          url: "watermark",
          data: { filename1: filename1, properties: properties },
          dataType: "json",
          success: function (response) {
            if (response.status === "success") {
              showCustomAlert(response.message);
              refreshFileLists();
            } else {
              showCustomAlert("错误: " + response.message);
            }
          },
          error: function (xhr, status, error) {
            showCustomAlert("请求失败: " + xhr.status + " " + error);
          }
        });
      });

      //刷新Select文件下拉列表
      function refreshFileLists() {
        let filename1 = $("#filename1").val();
        $.ajax({
          url: 'listFiles',
          type: 'GET',
          data: { filename1: filename1 },
          dataType: 'json',
          success: function (files) {
            const shpFiles = files.shp;
            const bmpFiles = files.bmp;
            const encryptedFiles = files.encrypted;
            const properties = files.properties;

            // 刷新 filename1 下拉框
            const $selectElement1 = $('#filename1');
            $selectElement1.empty(); // 清空现有选项
            $.each(shpFiles, function (index, file) {
              const $option = $('<option></option>').val(file).text(file);
              $selectElement1.append($option); // 添加新选项
            });

            // 刷新 filename2 下拉框
            const $selectElement2 = $('#filename2');
            $selectElement2.empty(); // 清空现有选项
            $.each(bmpFiles, function (index, file) {
              const $option = $('<option></option>').val(file).text(file);
              $selectElement2.append($option); // 添加新选项
            });

            // 刷新 decryptedSelect 下拉框
            const $selectElement3 = $('#selectedIndices');
            $selectElement3.empty(); // 清空现有选项
            $.each(encryptedFiles, function (index, file) {
              const $option = $('<option></option>').val(file).text(file);
              $selectElement3.append($option); // 添加新选项
            });
            // 刷新properties下拉框
            const $selectElement4 = $('#properties');
            $selectElement4.empty(); // 清空现有选项
            $.each(properties, function (index, file) {
              const $option = $('<option></option>').val(file).text(file);
              $selectElement4.append($option); // 添加新选项
            });

          },
          error: function (jqXHR, textStatus, errorThrown) {
            showCustomAlert('获取文件列表失败:' + errorThrown)
            console.error('获取文件列表失败:', errorThrown);
          }
        });
      }

      // $('#deleteButton').click(function() {
      //   $.ajax({
      //     url: '/deleteFiles',
      //     method: 'GET',
      //     dataType: 'json',
      //     success: function(data) {
      //       alert(data);
      //     },
      //     error: function(error) {
      //       alert(error);
      //     }
      //   });
      // });

      // 1. 全局配置优化
      const map = new ol.Map({
        target: 'map',
        layers: [
          new ol.layer.Tile({
            source: new ol.source.OSM({
              wrapX: false
            })
          })
        ],
        view: new ol.View({
          center: ol.proj.fromLonLat([0, 0]),
          zoom: 10,
          maxZoom: 18,
          extent: ol.proj.transformExtent([-180, -90, 180, 90], 'EPSG:4326', 'EPSG:3857')
        }),
        // 关闭不必要的渲染优化，适合大数据量场景
        loadTilesWhileAnimating: false,
        loadTilesWhileInteracting: false
      });

      let vectorLayer, vectorSource;
      const CACHE_EXPIRE_TIME = 24 * 60 * 60 * 1000; // 缓存有效期24小时
      const MAX_CACHE_SIZE = 5 * 1024 * 1024; // 5MB 最大缓存单个文件大小


// 3. 改进的缓存机制（解决存储配额问题）
      function getDataSize(data) {
        try {
          return new TextEncoder().encode(JSON.stringify(data)).length;
        } catch (e) {
          console.error('计算数据大小失败:', e);
          return 0;
        }
      }

      function getSortedCacheKeys() {
        const keys = [];
        for (let i = 0; i < localStorage.length; i++) {
          const key = localStorage.key(i);
          if (key.startsWith('shp_cache_')) {
            try {
              const item = JSON.parse(localStorage.getItem(key));
              keys.push({ key, timestamp: item.timestamp });
            } catch (e) {
              // 清理损坏的缓存
              localStorage.removeItem(key);
            }
          }
        }
        // 按时间戳排序，最早的在前面
        return keys.sort((a, b) => a.timestamp - b.timestamp);
      }

      function cleanupOldCache(neededSize) {
        const keys = getSortedCacheKeys();
        let totalFreed = 0;

        for (const { key } of keys) {
          const item = localStorage.getItem(key);
          const itemSize = getDataSize(item);
          localStorage.removeItem(key);
          totalFreed += itemSize;

          if (totalFreed >= neededSize) {
            break;
          }
        }
      }

      function clearAllCache() {
        const keys = getSortedCacheKeys();
        keys.forEach(({ key }) => localStorage.removeItem(key));
        console.log('已清除所有缓存');
      }

      function getCachedData(key) {
        try {
          const cached = localStorage.getItem(`shp_cache_${key}`);
          if (!cached) return null;

          const { data, timestamp } = JSON.parse(cached);
          // 检查缓存是否过期
          if (Date.now() - timestamp < CACHE_EXPIRE_TIME) {
            return data;
          }
          // 过期缓存清理
          localStorage.removeItem(`shp_cache_${key}`);
          return null;
        } catch (e) {
          console.error('获取缓存数据失败:', e);
          localStorage.removeItem(`shp_cache_${key}`); // 移除损坏的缓存
          return null;
        }
      }

      function cacheData(key, data) {
        try {
          // 检查数据大小
          const dataSize = getDataSize(data);

          // 如果数据太大，直接不缓存
          if (dataSize > MAX_CACHE_SIZE) {
            console.warn(`数据大小(${Math.round(dataSize/1024)}KB)超过最大缓存限制(${Math.round(MAX_CACHE_SIZE/1024)}KB)，不进行缓存`);
            return false;
          }

          // 尝试清理旧缓存以腾出空间
          cleanupOldCache(dataSize);

          // 再次尝试缓存
          localStorage.setItem(`shp_cache_${key}`, JSON.stringify({
            data,
            timestamp: Date.now(),
            size: dataSize
          }));
          console.log(`缓存成功，数据大小: ${Math.round(dataSize/1024)}KB`);
          return true;
        } catch (e) {
          if (e.name === 'QuotaExceededError') {
            console.warn('缓存空间不足，已尝试清理旧缓存但仍无法存储');
            // 作为最后的手段，清除所有缓存
            clearAllCache();
          } else {
            console.error('缓存失败:', e);
          }
          return false;
        }
      }

// 4. 主线程解析数据
      function parseFeatures(geojsonData) {
        try {
          const geojsonFormat = new ol.format.GeoJSON();
          const features = geojsonFormat.readFeatures(geojsonData, {
            featureProjection: 'EPSG:3857'
          });

          // 简化几何图形（关键优化）
          features.forEach(feature => {
            const geometry = feature.getGeometry();
            if (geometry) {
              // 简化精度根据实际需求调整
              feature.setGeometry(geometry.simplify(10));
            }
          });

          return features;
        } catch (error) {
          throw new Error(`解析失败: ${error.message}`);
        }
      }

// 5. 优化的加载逻辑
      $('#loadSHPBtn').on('click', function () {
        const filename = $('#filename1').val();
        if (!filename) {
          showCustomAlert('请先选择一个文件');
          return;
        }

        // 先检查缓存
        const cachedData = getCachedData(filename);
        if (cachedData) {
          try {
            showLoading();
            // 使用主线程解析缓存数据
            const features = parseFeatures(cachedData);
            updateVectorLayer(features);
            // showCustomAlert("从缓存加载成功");
            hideLoading();
            return;
          } catch (e) {
            console.error('缓存数据解析失败', e);
            // 移除损坏的缓存
            localStorage.removeItem(`shp_cache_${filename}`);
            hideLoading();
          }
        }

        // 无缓存则请求服务器
        showLoading();
        $.ajax({
          url: 'convert',
          type: 'POST',
          data: {
            filename: filename,
            // 后端优化：请求简化后的GeoJSON
            simplify: true,
            tolerance: 10 // 简化 tolerance，减少顶点数量
          },
          success: function (data) {
            try {
              // 尝试缓存，但不强制要求成功
              const cacheSuccess = cacheData(filename, data);
              if (!cacheSuccess) {
                // 可以选择在这里提示用户数据过大无法缓存
                // showCustomAlert("数据过大，未进行缓存以节省空间");
              }

              // 使用主线程解析
              const features = parseFeatures(data);
              updateVectorLayer(features);
              showCustomAlert("加载成功");
            } catch (e) {
              console.error('数据处理失败', e);
              showCustomAlert('数据解析失败: ' + e.message);
            } finally {
              hideLoading();
            }
          },
          error: function (xhr, status, error) {
            console.error('加载失败', error);
            showCustomAlert('加载失败，请重试: ' + error);
            hideLoading();
          }
        });
      });

// 更新矢量图层的通用方法
      function updateVectorLayer(features) {
        // 移除旧图层
        if (vectorLayer) {
          map.removeLayer(vectorLayer);
        }

        vectorSource = new ol.source.Vector({
          features: features,
          strategy: ol.loadingstrategy.bbox, // 视口内按需加载
          wrapX: false
        });

        vectorLayer = new ol.layer.Vector({
          source: vectorSource,
          style: new ol.style.Style({
            stroke: new ol.style.Stroke({
              color: 'red',
              width: 2
            })
          }),
          renderMode: 'image', // 大数据量时使用image模式渲染更快
          maxResolution: 8000
        });

        map.addLayer(vectorLayer);

        // 定位到数据范围
        if (features.length > 0) {
          map.getView().fit(vectorSource.getExtent(), {
            padding: [50, 50, 50, 50],
            duration: 500 // 缩短动画时间
          });
        }
      }

// 移除图层
      $('#removeSHPBtn').on('click', function() {
        if (vectorLayer) {
          map.removeLayer(vectorLayer);
          vectorLayer = null;
          vectorSource = null;
          showCustomAlert("地图已移除");
        } else {
          showCustomAlert("没有可移除的地图图层");
        }
      });
    });
//获取要素数量
    $('#localDecryptBtn').on('click', function () {
      const selectedIndices = [];
      const selectedValues = []; // 用于存储选中的内容
      const allOptions = [];
      const properties = $('#properties').val();
      // 遍历所有 <select> 的选项
      $('#selectedIndices option').each(function () {
        allOptions.push($(this).val());  // 将选项的值推入数组
      });
      $('#selectedIndices option:selected').each(function () {
        selectedIndices.push($(this).index());
        selectedValues.push($(this).val()); // 获取选中项的值
      });
      if (selectedIndices.length === 0) {
        showCustomAlert("请选择要解密的水印");
        return;
      }
      $.ajax({
        url: 'localDecryption',
        type: 'POST',
        dataType: 'json',
        data: JSON.stringify({
          selectedIndices: selectedIndices,
          selectedValues: selectedValues,
          allOptions: allOptions.join(","),
          properties: properties
        }),
        success: function (response) {
          if (response.status === "success") {
            showCustomAlert(response.message);
            refreshFileLists();
          } else {
            showCustomAlert("错误:" + response.message);
          }
        },
      });
    });
  </script>
</body>

</html>