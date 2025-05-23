// src/App.js
import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

import Login from './views/Login';
import Register from './views/Register';
import RegisterCompany from './views/RegisterCompany';
import Dashboard from './views/dashboard';
// 添加更多页面组件时导入

export default function App() {
  return (
    <Routes>
      {/* 默认跳转到登录页 */}
      <Route path="/" element={<Login />} />

      {/* 注册相关 */}
      <Route path="/register" element={<Register />} />
      <Route path="/register-company" element={<RegisterCompany />} />

      {/* 登录成功后的主页面 */}
      <Route path="/dashboard" element={<Dashboard />} />

      {/* 未匹配路径重定向 */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
