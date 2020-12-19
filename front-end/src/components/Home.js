// 不用class定义因为不需要state，直接display一下就完了

import React from 'react';
import { Button, Card, List, message, Tabs, Tooltip } from 'antd';
import { StarOutlined, StarFilled } from '@ant-design/icons';
import { addFavoriteItem, deleteFavoriteItem } from '../utils';

 
const { TabPane } = Tabs;
const tabKeys = {
  Streams: 'stream',
  Videos: 'videos',
  Clips: 'clips',
}
 
const processUrl = (url) => url
  .replace('%{height}', '252')
  .replace('%{width}', '480')
  .replace('{height}', '252')
  .replace('{width}', '480');
 
const renderCardTitle = (item, loggedIn, favs, favOnChange) => {
  const title = `${item.broadcaster_name} - ${item.title}`;

  const isFav = favs.find((fav) => fav.id === item.id); // find的作用就是遍历favs，看有没有member的id和item.id一样的，有就是true
  // 看当前列表里有没有user点击的东西， 有的话就unfill star，没有就fill star

  const favOnClick = () => {
    if (isFav) {
      deleteFavoriteItem(item).then(() => {
        favOnChange();
      }).catch(err => {
        message.error(err.message)
      })
 
      return;
    }
 
    addFavoriteItem(item).then(() => {
      favOnChange();
    }).catch(err => {
      message.error(err.message)
    })
  }


  return (
    <>
      {
        loggedIn &&
        <Tooltip title={isFav ? "Remove from favorite list" : "Add to favorite list"}> 
          <Button shape="circle" icon={isFav ? <StarFilled /> : <StarOutlined />} onClick={favOnClick} /> 
        </Tooltip> // tooltip：鼠标移上favorite图标，会显示comment
      }
      <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', width: 450 }}>
        <Tooltip title={title}>
          <span>{title}</span>
        </Tooltip>
      </div>
    </>
  )
}
 
const renderCardGrid = (data, loggedIn, favs, favOnChange) => {
  return (
    <List // 屏幕xs最小的时候显示一个，最大时候xl显示6个
      grid={{
        xs: 1,
        sm: 1,
        md: 2,
        lg: 2,
        xl: 2,
      }}
      dataSource={data}
      renderItem={item => ( // item是dataSource中的一个个内容
        <List.Item style={{ marginRight: '20px' }}>
          <Card // ant design，用来放video的
            title={renderCardTitle(item, loggedIn, favs, favOnChange)} // 需要loggedIn因为需要知道视频是否被favorite过
          >
            <a href={item.url} target="_blank" rel="noopener noreferrer">
              <img 
                alt="Placeholder"
                src={processUrl(item.thumbnail_url)}
              />
            </a>
          </Card>
        </List.Item>
      )}
    />
  )
}
 
const Home = ({ resources, loggedIn, favoriteItems, favoriteOnChange }) => { // 结构语法，不用props，而是直接map props到具体parameter {resources, loggedIn}
  const { VIDEO, STREAM, CLIP } = resources;
  const { VIDEO: favVideos, STREAM: favStreams, CLIP: favClips} = favoriteItems;
 
  return (
    <Tabs 
      defaultActiveKey={tabKeys.Streams} 
    >
      <TabPane tab="Streams" key={tabKeys.Streams} style={{ height: '680px', overflow: 'auto' }} forceRender={true}>
        {renderCardGrid(STREAM, loggedIn, favStreams, favoriteOnChange)}
      </TabPane>
      <TabPane tab="Videos" key={tabKeys.Videos} style={{ height: '680px', overflow: 'auto' }} forceRender={true}>
        {renderCardGrid(VIDEO, loggedIn, favVideos, favoriteOnChange)}
      </TabPane>
      <TabPane tab="Clips" key={tabKeys.Clips} style={{ height: '680px', overflow: 'auto' }} forceRender={true}>
        {renderCardGrid(CLIP, loggedIn, favClips, favoriteOnChange)}
      </TabPane>
    </Tabs>
  );
}
 
export default Home;