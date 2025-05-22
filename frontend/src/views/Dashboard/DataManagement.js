import React, { useState, useEffect } from 'react';
import ReactPaginate from 'react-paginate';
import axios from 'axios';
import * as XLSX from 'xlsx'; 
import { saveAs } from 'file-saver'; 
import '../../assets/styles/DataManagement.css';

const itemsPerPage = 10;

export default function FinancialDashboard() {
    const [view, setView] = useState('income');
    const [pageByView, setPageByView] = useState({ income: 0 });
    const [incomeData, setIncomeData] = useState([]);

    useEffect(() => {
        axios.get('http://localhost:8085/api/transactions', {
            withCredentials: true
        })
        .then(res => {
            console.log("✅ 成功获取数据：", res.data);
            const incomeOnly = res.data.filter(txn => txn.transactionType === 'INCOME');
            setIncomeData(incomeOnly);
        })
        .catch(err => {
            console.error("❌ 获取失败：", err);
        });
    }, []);

    const handleViewChange = (newView) => setView(newView);
    const handlePageClick = (selectedItem) => {
        setPageByView(prev => ({ ...prev, [view]: selectedItem.selected }));
    };

    const handleExport = () => {
        const worksheetData = incomeData.map(txn => ({
            Date: txn.transactionDate,
            Description: txn.description,
            Amount: txn.amount,
            Currency: txn.currency,
            PaymentMethod: txn.paymentMethod,
            Reference: txn.referenceNumber
        }));

        const worksheet = XLSX.utils.json_to_sheet(worksheetData);
        const workbook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(workbook, worksheet, "Income Report");

        const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
        const blob = new Blob([excelBuffer], { type: "application/octet-stream" });
        saveAs(blob, "Income_Report.xlsx");
    };

    const pageCount = Math.ceil(incomeData.length / itemsPerPage);
    const currentPage = pageByView[view];
    const currentData = incomeData.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage);

    return (
        <div className="p-6">
            {/* 顶部按钮区域 / Top buttons */}
            <div className="flex justify-between items-center mb-4">
                <div className="flex gap-4">
                    <button className={`custom-button ${view === 'income' ? 'active' : 'inactive'}`} onClick={() => handleViewChange('income')}>
                        Income Report
                    </button>
                </div>
                <div>
                    <button className="custom-button" onClick={handleExport}>
                        Export to Excel
                    </button>
                </div>
            </div>

            {/* 数据表格 / Table */}
            <div className="overflow-x-auto mt-2">
                <table className="min-w-full border border-gray-300 rounded-lg text-lg">
                    <thead className="bg-blue-100 text-gray-700">
                        <tr>
                            <th className="border px-6 py-4">Date</th>
                            <th className="border px-6 py-4">Description</th>
                            <th className="border px-6 py-4">Amount</th>
                            <th className="border px-6 py-4">Currency</th>
                            <th className="border px-6 py-4">Payment Method</th>
                            <th className="border px-6 py-4">Reference</th>
                        </tr>
                    </thead>
                    <tbody>
                        {currentData.length === 0 ? (
                            <tr>
                                <td colSpan={6} className="text-center py-4 text-gray-500">暂无收入数据 / No income data available.</td>
                            </tr>
                        ) : (
                            currentData.map(txn => (
                                <tr key={txn.transactionId} className="hover:bg-gray-50 transition">
                                    <td className="border px-6 py-4">{txn.transactionDate}</td>
                                    <td className="border px-6 py-4">{txn.description}</td>
                                    <td className="border px-6 py-4">{txn.amount.toLocaleString()}</td>
                                    <td className="border px-6 py-4">{txn.currency}</td>
                                    <td className="border px-6 py-4">{txn.paymentMethod}</td>
                                    <td className="border px-6 py-4">{txn.referenceNumber}</td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* 分页 / Pagination */}
            <div className="flex justify-center mt-6">
                <ReactPaginate
                    previousLabel={'<'}
                    nextLabel={'>'}
                    breakLabel={null}
                    pageCount={pageCount}
                    marginPagesDisplayed={1}
                    pageRangeDisplayed={3}
                    onPageChange={handlePageClick}
                    containerClassName="custom-pagination-container"
                    pageClassName="custom-page"
                    activeClassName="custom-active-page"
                    previousClassName="custom-prev-btn"
                    nextClassName="custom-next-btn"
                    disabledClassName="custom-disabled-btn"
                />
            </div>
        </div>
    );
}
