import React, { useState } from 'react';
import ReactPaginate from 'react-paginate';
import '../../assets/styles/DataManagement.css';

const incomeData = [
    {
        item: 'Pledges',
        currentMonth: 37110.00,
        previousMonth: 47962.32,
        yearToDate: 37110.00,
        recoveryFromJACM: null,
        actualYTD: 37110.00,
        budgetYTD: 36000.00,
        variance: 1110.00,
        fullYearBudget: 432000.00
    },
    {
        item: 'Donations',
        currentMonth: 21500.00,
        previousMonth: 23980.45,
        yearToDate: 21500.00,
        recoveryFromJACM: 1000.00,
        actualYTD: 22500.00,
        budgetYTD: 25000.00,
        variance: -2500.00,
        fullYearBudget: 300000.00
    },
    {
        item: 'Grants',
        currentMonth: 104500.00,
        previousMonth: 112000.00,
        yearToDate: 104500.00,
        recoveryFromJACM: 5000.00,
        actualYTD: 109500.00,
        budgetYTD: 100000.00,
        variance: 9500.00,
        fullYearBudget: 1200000.00
    },
    {
        item: 'Fundraising',
        currentMonth: 8000.00,
        previousMonth: 9800.00,
        yearToDate: 8000.00,
        recoveryFromJACM: null,
        actualYTD: 8000.00,
        budgetYTD: 15000.00,
        variance: -7000.00,
        fullYearBudget: 180000.00
    },
    {
        item: 'Sponsorships',
        currentMonth: 54000.00,
        previousMonth: 56000.00,
        yearToDate: 54000.00,
        recoveryFromJACM: 3000.00,
        actualYTD: 57000.00,
        budgetYTD: 55000.00,
        variance: 2000.00,
        fullYearBudget: 660000.00
    },
    {
        item: 'Membership Fees',
        currentMonth: 11800.00,
        previousMonth: 12000.00,
        yearToDate: 11800.00,
        recoveryFromJACM: null,
        actualYTD: 11800.00,
        budgetYTD: 12000.00,
        variance: -200.00,
        fullYearBudget: 144000.00
    }
];

const balanceSheetData = [
    {
        item: 'General Fund Reserve',
        balanceAsAt: 1037513.57,
        previousMonth: null,
        asAt31Dec24: null,
        currentYearSurplus: 0,
        total: 1037513.57,
    },
    {
        item: 'Other Funds',
        subItems: [
            { subItem: 'C.O.M.E. Funds', amount: 104693.33 },
            { subItem: 'Sinking Fund', amount: 1855.0 },
            { subItem: 'Reno Cap Fund', amount: 74560.35 },
            { subItem: 'FFE Cap Fund', amount: 75058.17 },
        ],
    },
    {
        item: 'Total Reserves and Funds',
        balanceAsAt: 1293680.42,
        previousMonth: null,
        asAt31Dec24: null,
        currentYearSurplus: 0,
        total: 1293680.42,
    },
    {
        item: 'Fixed Assets',
        subItems: [
            { subItem: 'Plant & Equipment', amount: 354616.2 },
            { subItem: 'Less Accumulated Depreciation', amount: -117837.93 },
        ],
    },
    {
        item: 'Current Assets',
        subItems: [
            { subItem: 'Sundry Debtors', amount: 15030.77 },
            { subItem: 'Deposits', amount: 80.0 },
            { subItem: 'Prepayments', amount: 16537.69 },
            { subItem: 'Fixed Deposit - HL Finance', amount: 836027.21 },
            { subItem: 'Cash & Bank Balances - DBS Bank', amount: 394588.36 },
        ],
    },
    {
        item: 'Liabilities',
        subItems: [
            { subItem: 'Sundry Creditors', amount: 1890.0 },
            { subItem: 'Accruals', amount: 187535.6 },
            { subItem: 'Contra', amount: 8778.28 },
            { subItem: 'Advance Receipts', amount: 7158.0 },
        ],
    },
    {
        item: 'Total Net Assets',
        balanceAsAt: 1293680.42,
        previousMonth: null,
        asAt31Dec24: null,
        currentYearSurplus: 0,
        total: 1293680.42,
    },
];

const itemsPerPage = 5;

export default function FinancialDashboard() {
    const [view, setView] = useState('income');
    const [pageByView, setPageByView] = useState({ income: 0, balance: 0 });

    const handleViewChange = (newView) => {
        setView(newView);
    };

    const handlePageClick = (selectedItem) => {
        setPageByView(prev => ({ ...prev, [view]: selectedItem.selected }));
    };

    const pageCount = view === 'income'
        ? Math.ceil(incomeData.length / itemsPerPage)
        : Math.ceil(balanceSheetData.length / itemsPerPage);

    const currentPage = pageByView[view];
    const currentData = view === 'income'
        ? incomeData.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage)
        : balanceSheetData.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage);

    return (
        <div className="p-6">
            {/* Header Buttons */}
            <div className="flex gap-4">
                <button
                    className={`custom-button ${view === 'income' ? 'active' : 'inactive'}`}
                    onClick={() => handleViewChange('income')}
                >
                    Income Report
                </button>
                <button
                    className={`custom-button ${view === 'balance' ? 'active' : 'inactive'}`}
                    onClick={() => handleViewChange('balance')}
                >
                    Balance Sheet
                </button>
            </div>

            <div className="flex gap-3 ml-auto">
                <button
                    className="custom-button"
                    onClick={() => console.log('Data Analysis clicked')}
                >
                    Data Analysis
                </button>
                <button
                    className="custom-button"
                    onClick={() => console.log('Report Export clicked')}
                >
                    Report Export
                </button>
            </div>

            {/* Table Content */}
            {view === 'income' ? (
                <div className="overflow-x-auto">
                    <table className="min-w-full border border-gray-300 rounded-lg text-lg">
                        <thead className="bg-blue-100 text-gray-700">
                            <tr>
                                <th className="border px-6 py-4">Item</th>
                                <th className="border px-6 py-4">Current Month</th>
                                <th className="border px-6 py-4">Previous Month</th>
                                <th className="border px-6 py-4">Year To Date</th>
                                <th className="border px-6 py-4">Recovery from JACM</th>
                                <th className="border px-6 py-4">Actual YTD</th>
                                <th className="border px-6 py-4">Budget YTD</th>
                                <th className="border px-6 py-4">Variance</th>
                                <th className="border px-6 py-4">Full Year Budget</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentData.length === 0 ? (
                                <tr>
                                    <td colSpan={9} className="text-center py-4 text-gray-500">No income data available.</td>
                                </tr>
                            ) : (
                                currentData.map(row => (
                                    <tr key={row.item} className="hover:bg-gray-50 transition">
                                        <td className="border px-6 py-4">{row.item}</td>
                                        <td className="border px-6 py-4">{row.currentMonth.toLocaleString()}</td>
                                        <td className="border px-6 py-4">{row.previousMonth ? row.previousMonth.toLocaleString() : '-'}</td>
                                        <td className="border px-6 py-4">{row.yearToDate.toLocaleString()}</td>
                                        <td className="border px-6 py-4">{row.recoveryFromJACM ? row.recoveryFromJACM.toLocaleString() : '-'}</td>
                                        <td className="border px-6 py-4">{row.actualYTD.toLocaleString()}</td>
                                        <td className="border px-6 py-4">{row.budgetYTD.toLocaleString()}</td>
                                        <td className={`border px-6 py-4 ${row.variance < 0 ? 'text-red-500' : 'text-green-600'}`}>
                                            {row.variance.toLocaleString()}
                                        </td>
                                        <td className="border px-6 py-4">{row.fullYearBudget.toLocaleString()}</td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div className="overflow-x-auto">
                    <table className="min-w-full border border-gray-300 rounded-lg text-lg">
                        <thead className="bg-blue-100 text-gray-700">
                            <tr>
                                <th className="border px-6 py-4">Item</th>
                                <th className="border px-6 py-4">Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentData.map((row, index) => (
                                row.subItems ? (
                                    <React.Fragment key={index}>
                                        <tr className="bg-gray-100 font-semibold">
                                            <td className="border px-6 py-4" colSpan={2}>{row.item}</td>
                                        </tr>
                                        {row.subItems.map((sub, subIndex) => (
                                            <tr key={subIndex}>
                                                <td className="border px-6 py-4 pl-8">{sub.subItem}</td>
                                                <td className="border px-6 py-4">{sub.amount.toLocaleString()}</td>
                                            </tr>
                                        ))}
                                    </React.Fragment>
                                ) : (
                                    <tr key={index}>
                                        <td className="border px-6 py-4">{row.item}</td>
                                        <td className="border px-6 py-4">{row.total?.toLocaleString() ?? '-'}</td>
                                    </tr>
                                )
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Pagination */}
            <div className="flex justify-center mt-6">
      <ReactPaginate
        previousLabel={'<'}
        nextLabel={'>'}
        breakLabel={null} // 去掉省略号
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
