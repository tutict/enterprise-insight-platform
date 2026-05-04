import { useSettingsPage } from '../hooks/useSettingsPage'
import SettingsView from '../components/SettingsView'

export default function SettingsContainer() {
  const settings = useSettingsPage()

  return <SettingsView {...settings} />
}
