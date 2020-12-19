import { Button, Form, Input, message, Modal } from 'antd';
import React from 'react';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { login } from '../utils';
 
class Login extends React.Component {
  state = {
    displayModal: false
  }
 
  handleCancel = () => {
    this.setState({
      displayModal: false,
    })
  }
 
  signinOnClick = () => {
    this.setState({
      displayModal: true,
    })
  }
 
  onFinish = (data) => {
    login(data) // login来自于utils.js
      .then((data) => {
        this.setState({
          displayModal: false,
        })
        message.success(`Welcome back, ${data.name}`);
        this.props.onSuccess();
      }).catch((err) => {
        message.error(err.message);
      })
  }
 
  render = () => { // 点击this.signinOnClick后， 会setState displayModel为true，然后Model里43行变成 visible
    return (
      <>
        <Button shape="round" onClick={this.signinOnClick} style={{ marginRight: '20px' }}> 
        Login</Button> 
        <Modal
          title="Log in"
          visible={this.state.displayModal}
          onCancel={this.handleCancel}
          footer={null} // 不显示底下的cancel和ok button
          destroyOnClose={true}
        >
          <Form // 这是一个ant design里的login界面，可以收集username和password
            name="normal_login"
            onFinish={this.onFinish}
            preserve={false}
          >
            <Form.Item
              name="user_id"
              rules={[{ required: true, message: 'Please input your Username!' }]}
            >
              <Input prefix={<UserOutlined />} placeholder="Username" />
            </Form.Item>
            <Form.Item
              name="password"
              rules={[{ required: true, message: 'Please input your Password!' }]}
            >
              <Input
                prefix={<LockOutlined />}
                placeholder="Password"
              />
            </Form.Item>
 
            <Form.Item>
              <Button type="primary" htmlType="submit">
                Login</Button>
            </Form.Item>
          </Form>
        </Modal>
      </>
    )
  }
}
 
export default Login;
