import { Link } from 'umi';

export default function () {
  return (
    <div style={{ padding: 32, maxWidth: 600 }}>
      <h1>组件测试页</h1>
      <ul style={{ fontSize: 16, lineHeight: 2.5 }}>
        <li>
          <Link to="/test/views">View 组件测试</Link>
        </li>
        <li>
          <Link to="/test/fields">RemoteSelect 系列组件测试</Link>
        </li>
      </ul>
    </div>
  );
}
